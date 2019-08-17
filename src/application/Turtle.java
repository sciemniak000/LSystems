package application;


import java.util.Stack;

import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class Turtle {
	private Canvas canvas;
	private double x, y;
	private Stack<Double> tmpX,tmpY; 
 
	private double alpha;
	private boolean isPenDown;
	private GraphicsContext context;
	private Task<Void> task;

	public Turtle (Canvas canvas, Task<Void> task) {
		this.canvas = canvas;
		this.task = task;
		context = canvas.getGraphicsContext2D();
		x = 0;
		y = 0;
		alpha = 90;
		isPenDown = true;
		tmpX = new Stack<>();
		tmpY = new Stack<>();
		
	}

	public void moveTo(double newX, double newY) {
		x = newX;
		y = newY;
	}
	
	public void remember() {
		tmpX.push(x);
		tmpY.push(y);
	}
	
	public void goBack() {
		x = tmpX.pop();
		y = tmpY.pop();
	}	
	
	public void forwardTo(double newX, double newY) {
		if(task != null && task.isCancelled()){
			throw new ThreadDeath();
		}
		if (isPenDown) {
			context.strokeLine(x, y, newX, newY);			
		}
		x = newX;
		y = newY;
	}

	public void forward(double n) {
		if(task != null && task.isCancelled()){
			throw new ThreadDeath();
		}
		double oldX = x;
		double oldY = y;
		x = x + n * Math.cos(alpha * Math.PI / 180);
		y = y - n * Math.sin(alpha * Math.PI / 180);
		if (isPenDown) {
			context.strokeLine(oldX, oldY, x, y);	
		}
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setDirection(double alpha) {
		this.alpha = alpha;
	}


	public void left(double beta) {
		alpha = alpha + beta/10;
	}

	
	public void right(double beta) {
		alpha = alpha - beta/10;
	}

	
	public double getDirection() {
		return alpha;
	}


	public void penUp() {
		isPenDown = false;
	}


	public void penDown() {
		isPenDown = true;
	}

	
	public double getWidth() {
		return canvas.getWidth();
	}

	
	public double getHeight() {
		return canvas.getHeight();
	}

}