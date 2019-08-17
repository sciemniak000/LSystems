package application;


import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light.Point;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class Main extends Application {	
	private Task<Void> drawTreeTask;
	private Group canvasRoot;
	private static JFXSpinner spinner;
	private static StackPane root = new StackPane();
	final double canvasWidth = 600;
	final double canvasHeight = 600;
	final double sceneWidth = 1200;
	final double sceneHeight = 700;
	final Rectangle selection = new Rectangle();
    final Point anchor = new Point();
    
    boolean dragFlag = false;

    int clickCounter = 0;

    ScheduledThreadPoolExecutor executor;

    ScheduledFuture<?> scheduledFuture;

    public Main() {
        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
    }
    
	public static void main(String[] args) {
		spinner = new JFXSpinner();
		
		
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		
		
		BorderPane layout = new BorderPane();	
		StackPane holder = new StackPane();
		spinner.setRadius(20);
        spinner.setVisible(false);

		holder.getChildren().addAll(addCanvas(),spinner);
        holder.setStyle("-fx-background-color: rgba(0, 100, 100, 0.5);");
		layout.setLeft(makeInput(stage));
		layout.setCenter(holder);		
		canvasRoot.setOnMouseDragged(new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent e) {
	                if (e.getButton().equals(MouseButton.PRIMARY) && !e.isControlDown()) {
	                    dragFlag = true;
	                }
	            }
	        });
		
		  canvasRoot.setOnMouseClicked(new EventHandler<MouseEvent>() {
	            @Override
	            public void handle(MouseEvent e) {
	                if (e.getButton().equals(MouseButton.PRIMARY) && !e.isControlDown()) {
	                    if (!dragFlag) {
	                        if (e.getClickCount() == 1) {
	                            scheduledFuture = executor.schedule(() -> singleClickAction(), 250, TimeUnit.MILLISECONDS);
	                            spinner.setVisible(true);
	                        } else if (e.getClickCount() > 1) {
	                            if (scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone()) {
	                                scheduledFuture.cancel(false);
	                                doubleClickAction();
		                            spinner.setVisible(false);

	                            }
	                        }
	                    }
	                    dragFlag = false;
	                }
	                
	                else if(e.getButton().equals(MouseButton.SECONDARY) && !e.isControlDown()){
	                	Thread t = drawTree();
	            		try {
	            			t.join();
	            		} catch (Exception er) {
	            			er.printStackTrace();
	            		}
	            		
						Rewriter.get_rewriter().previousStep();
				        spinner.setVisible(false);

						

					}
				
	            }
	        });
	
	
		 canvasRoot.setOnMousePressed(event -> {	
			 MouseButton button = event.getButton();
	     if(button == MouseButton.MIDDLE ) {
			 anchor.setX(event.getX());
	         anchor.setY(event.getY());
	         selection.setX(event.getX());
	         selection.setY(event.getY());
	         selection.setFill(Color.rgb(0, 0, 255, 0.2)); // transparent 
	         selection.setStroke(Color.BLUE); // border
	         selection.getStrokeDashArray().add(10.0);
	         canvasRoot.getChildren().add(selection);
	     }
		 }); 	
		 canvasRoot.setOnMouseDragged(event -> {
			  	
			 	double width = Math.abs(event.getX() - anchor.getX());
			 	double height = Math.abs(event.getY() - anchor.getY());
			 	double delta = Math.abs(width - height);
			 	if (height < width) {
			 		 	selection.setWidth(height);
			            selection.setHeight(height);
			            selection.setX(Math.min(anchor.getX(), (event.getX()+delta)));
			            selection.setY(Math.min(anchor.getY(), event.getY()));
			 	}else {
			 		selection.setWidth(width);
		            selection.setHeight(width);
		            selection.setX(Math.min(anchor.getX(), event.getX()));
		            selection.setY(Math.min(anchor.getY(), (event.getY()+delta)));
			 	}
	           
	            
	        }); 
		 canvasRoot.setOnMouseReleased(event -> {	
			 MouseButton button = event.getButton();
		     if(button == MouseButton.MIDDLE) {
			    rescale();
	            canvasRoot.getChildren().remove(selection);
	            selection.setWidth(0);
	            selection.setHeight(0);
		     }
	        });
		Rectangle clip = new Rectangle(canvasWidth, canvasHeight);
		clip.setLayoutX(0);
		clip.setLayoutY(0);
		    
		canvasRoot.setClip(clip);
		root.getChildren().add(layout);
		JFXDecorator decorator = new JFXDecorator(stage, root);
		decorator.setCustomMaximize(true);
		stage.setScene(new Scene(decorator, sceneWidth, sceneHeight));
		stage.setTitle("L-system");
		stage.show();
	}

	private void rescale() {
		double scale = canvasWidth * canvasWidth/selection.getWidth();
	 	canvasRoot.setScaleX(scale);
	 	canvasRoot.setScaleY(scale);
//        canvasRoot.setLayoutX(selection.getX());
//        canvasRoot.setLayoutY(selection.getY());
        drawTree();
	}
	private FlowPane makeInput(Stage stage) {
		FlowPane input = new FlowPane();
		StackPane temp = new StackPane();
		input.setPadding(new Insets(20, 10, 10, 10));
		input.setVgap(8);
	    input.setHgap(4);
		JFXTextArea in_area = new JFXTextArea();		
		JFXRippler rippler = new JFXRippler(in_area);
		temp.getChildren().addAll(in_area,rippler);
		in_area.setLabelFloat(true);
		in_area.setPromptText("Add Your Own Configuration");
		JFXButton get_input = new JFXButton("Get input");
		JFXButton get_file = new JFXButton("Get file");
		JFXButton get_preset1 = new JFXButton("Preset 1");
		JFXButton get_preset2 = new JFXButton("Preset 2");
		JFXButton get_preset3 = new JFXButton("Preset 3");
		JFXButton get_preset4 = new JFXButton("Preset 4");
		JFXButton get_preset5 = new JFXButton("Preset 5");
		JFXButton get_preset6 = new JFXButton("Preset 6");
		JFXButton get_preset7 = new JFXButton("Preset 7");


		Stream.of(get_input, get_file, get_preset1, get_preset2,get_preset3, get_preset4,get_preset5, get_preset6,get_preset7).forEach(button -> 
	    button.setStyle("-fx-padding: 0.7em 0.57em;\r\n" + 
				"    -fx-font-size: 14px;\r\n" + 
				"    -jfx-button-type: RAISED;\r\n" + 
				"    -fx-background-color: rgb(0,0,0);\r\n" + 
				"    -fx-pref-width: 500;\r\n" + 
				"    -fx-text-fill: WHITE;"));
			
		get_input.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        String new_text = in_area.getText();
		        drawTree();
		        if (new_text != null) {
				try {
					Parser.getParser().parse(new_text);
			        spinner.setVisible(false);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        
		        in_area.clear();
		        }
		    }
		});
		
		get_file.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	 FileChooser fileChooser = new FileChooser();
				 fileChooser.setTitle("Open Resource File");
				 fileChooser.getExtensionFilters().addAll(
				         new ExtensionFilter("Text Files", "*.txt"));
				 File selectedFile = fileChooser.showOpenDialog(stage);
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
		get_preset1.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {		    	
				 File selectedFile = new File("resources/miss.txt");
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
		get_preset2.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {		    	
				 File selectedFile = new File("resources/miss2.txt");
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
		get_preset3.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {		    	
				 File selectedFile = new File("resources/miss3.txt");
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
		get_preset4.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {		    	
				 File selectedFile = new File("resources/miss4.txt");
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
		get_preset5.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {		    	
				 File selectedFile = new File("resources/miss5.txt");
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
		get_preset6.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {		    	
				 File selectedFile = new File("resources/miss6.txt");
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
		get_preset7.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {		    	
				 File selectedFile = new File("resources/miss7.txt");
				 if (selectedFile != null) {					 
					 try {
						drawTree();
						Parser.getParser().parse(selectedFile);
				        spinner.setVisible(false);

					} catch (IOException e1) {						 
						e1.printStackTrace();
					}
				 }
		    }
		});
		
	    input.getChildren().addAll(temp,get_input,get_file,get_preset1, get_preset2,get_preset3, get_preset4,get_preset5, get_preset6,get_preset7);
		in_area.setStyle("-fx-pref-width: 500;\r\n" + 
						"-fx-background-color:WHITE");

		return input;	 
	}
	
	private Node addCanvas() {
		FlowPane pane = new FlowPane();
		canvasRoot = new Group();
		drawTree();
		pane.getChildren().add(canvasRoot);
		//pane.setStyle("-fx-background-color:RED");
		pane.setMaxSize(500, 500);
		return pane;
	}


	private Thread drawTree(){
		if(drawTreeTask != null && drawTreeTask.isDone()){
			drawTreeTask.cancel(true);
		}
		drawTreeTask = new Task<Void>() {
			@Override protected Void call() throws Exception {
				try{
				Platform.runLater(new Runnable() {
					@Override public void run() {
						canvasRoot.getChildren().clear();
					}
				});
				final Canvas canvas = new Canvas(canvasWidth, canvasHeight);					
				GraphicsContext gcontext = canvas.getGraphicsContext2D();
				gcontext.setLineWidth(0.3);
				
				Turtle turtleinstance = new Turtle(canvas, this);	
				
				Rewriter.get_rewriter().setTurtle(turtleinstance);
				
				Platform.runLater(new Runnable() {
					@Override public void run() {
						
						canvasRoot.getChildren().add(canvas);
					}
				});
				
				} catch (ThreadDeath e) { 
				} catch(Throwable e){
					e.printStackTrace(System.err);
				}
				return null;
			}
			 @Override
		        protected void succeeded() {
		        }

		};
		Thread thread = new Thread(drawTreeTask);
		thread.setDaemon(true);
		spinner.setVisible(true);
		thread.start();
		return thread;
	}
	
	public void stop() {
		spinner.setVisible(false);
        executor.shutdown();
    }

    private void singleClickAction() {
        spinner.setVisible(true);
        try {
        	Thread t = drawTree();

			t.join();
       
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Rewriter.get_rewriter().nextStep();
        spinner.setVisible(false);
		
	}

    private void doubleClickAction() {
    	canvasRoot.setScaleX(1);
	 	canvasRoot.setScaleY(1);
    }

    public static void showInfoDialog(String title, String text){
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text(title));
        content.setBody(new Text(text));
        JFXDialog dialog = new JFXDialog(root, content, JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton("Okay");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();                
            }
        });
        content.setActions(button);
        dialog.show();
    }
	
}