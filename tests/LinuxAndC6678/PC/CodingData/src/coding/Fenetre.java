package coding;

import javax.swing.JFrame;

@SuppressWarnings("serial")

public class Fenetre extends JFrame{

	String TITLE = new String("");
	int WIDTH, HEIGHT;
	
	public void getValue(String titre, int width, int height){
		TITLE = titre;
		WIDTH = width;
		HEIGHT = height;
	}
	
	public void init(){
    	this.setTitle(TITLE);
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);              
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
	}
}