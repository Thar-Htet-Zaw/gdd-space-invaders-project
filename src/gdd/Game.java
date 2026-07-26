package gdd;

import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import javax.swing.JFrame;

public class Game extends JFrame  {

    TitleScene titleScene;
    Scene1 scene1;
    Scene2 scene2;

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        scene2 = new Scene2(this);
        initUI();
        loadTitle();
        //loadScene2();
    }

    private void initUI() {

        setTitle("Void Runner");
        setSize(Global.BOARD_WIDTH, Global.BOARD_HEIGHT);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

    }

    public void loadTitle() {
        getContentPane().removeAll();
        //add(new Title(this));
        add(titleScene);
        titleScene.start();
        revalidate();
        repaint();
        titleScene.requestFocusInWindow();
    }

    public void loadScene1() {
        getContentPane().removeAll();
        add(scene1);
        if (titleScene != null) {
            titleScene.stop(); 
        }
        scene1.start();
        revalidate();
        repaint();
        scene1.requestFocusInWindow();
    }

    public void loadScene2() {
        if (scene1 != null) {
            scene1.stop();
            scene1.setVisible(false);
            getContentPane().remove(scene1); 
        }

        getContentPane().removeAll(); 
        
        scene2.setVisible(true); 
        add(scene2);

        revalidate();
        repaint();

        scene2.start();
        scene2.setFocusable(true);
        scene2.requestFocusInWindow();
    }
}