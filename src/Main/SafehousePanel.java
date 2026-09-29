package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class SafehousePanel extends JPanel implements KeyListener {

    private Player player;
    private BufferedImage background;
    private CollisionChecker collision = new CollisionChecker();

    private boolean[] keys = new boolean[256];

    private boolean showingDialogue = true;
    private boolean showingLetter = false;
    private boolean postLetterDialogue = false;
    boolean notesRead = false;

    private boolean nearLetter = false;
    private boolean nearDoor = false;

    private int dialogueIndex = 0;
    private int afterIndex = 0;

    private final String[] introDialogue = {
            "...Where am I?",
            "My chest... it still hurts.",
            "How long was I unconscious...?",
            "I should look around."
    };

    private final String[] letterText = {
            "Ethan,",
            "If you're reading this, you're alive.",
            "Raven City has fallen.",
            "Find the Chief.",
            "He knows everything.",
            "",
            "- A Friend"
    };

    private final String[] afterLetter = {
            "The Chief...?",
            "What the hell happened to this city?",
            "I need answers.",
            "Objective Updated:",
            "Leave the Safehouse."
    };

    public SafehousePanel() {

        setFocusable(true);
        addKeyListener(this);

        try {
            background = ImageIO.read(
                getClass().getResource("/assets/safehouse_bg.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        player = new Player(245, 205);

        initCollision();

        Timer timer = new Timer(16, e -> {
            update();
            repaint();
        });

        timer.start();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void initCollision() {
        collision.clear();

   

        // =========================
        // ROOM WALLS
        // =========================

     // ROOM WALLS
        collision.addWall(0, 0, 768, 24);      // Top
        collision.addWall(0, 0, 24, 576);      // Left
        collision.addWall(744, 0, 24, 576);    // Right

        // Fix top-left leak
        collision.addWall(24, 24, 96, 96);

        // Bottom split (door opening)
        collision.addWall(0,552,300,24);
        collision.addWall(468,552,300,24);

        // Door pillars
        collision.addWall(300,470,24,82);
        collision.addWall(444,470,24,82);

     // ========= FURNITURE =========

     // =========================
     // BED
     // =========================
     collision.addWall(58, 58, 138, 132);

     // Nightstand (wider so it actually blocks)
     collision.addWall(182, 60, 56, 64);

     // =========================
     // CENTER TABLE
     // =========================
     // Smaller top collision so Ethan can get close
     collision.addWall(336, 188, 108, 58);

     // =========================
     // TOP RIGHT CRATES
     // =========================
     collision.addWall(596, 36, 92, 84);

     // =========================
     // BOTTOM LEFT CRATE
     // =========================
     collision.addWall(44, 424, 84, 58);

     // =========================
     // BOTTOM RIGHT BOXES
     // =========================
     collision.addWall(664, 402, 46, 84);}
     
    private void update(){

        if(!showingDialogue && !showingLetter && !postLetterDialogue){

            int oldX = player.x;
            int oldY = player.y;

            player.update(keys);

         // ===== SAFEHOUSE ROOM BOUNDARY =====
            int MIN_X = 134;//left
            int MAX_X = 600;   // RIGHT

            int MIN_Y = 50;    // TOP
            int MAX_Y = 400;

            if (player.x < MIN_X) player.x = MIN_X;
            if (player.x > MAX_X) player.x = MAX_X;
            if (player.y < MIN_Y) player.y = MIN_Y;
            if (player.y > MAX_Y) player.y = MAX_Y;
            
            
            // Furniture / wall collision
            if(collision.isColliding(player.getBounds())){
                player.x = oldX;
                player.y = oldY;
            }
        }

        Rectangle letter = new Rectangle(345,205,95,80);
        nearLetter = player.getBounds().intersects(letter);

        Rectangle door = new Rectangle(334,360,100,55);
        nearDoor = player.getBounds().intersects(door);
    }

    @Override
    protected void paintComponent(Graphics g){

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if(background != null)
            g2.drawImage(background,0,0,getWidth(),getHeight(),null);

        player.draw(g2);

     // READ PROMPT
        if(nearLetter && !showingDialogue && !showingLetter && !postLetterDialogue){

            g2.setColor(new Color(255,255,255,230));
            g2.fillRoundRect(333,170,120,30,8,8);

            g2.setColor(Color.BLACK);
            g2.drawString("E - Read",360,190);
        }

        // EXIT PROMPT
        if(nearDoor && !showingDialogue && !showingLetter && !postLetterDialogue){

            g2.setColor(new Color(255,255,255,230));
            g2.fillRoundRect(324,325,120,30,8,8);

            g2.setColor(Color.BLACK);
            g2.drawString("E - Exit",351,345);
        }

        // INTRO DIALOGUE
        if(showingDialogue){

            g2.setColor(new Color(0,0,0,170));
            g2.fillRect(0,0,getWidth(),getHeight());

            g2.setColor(new Color(20,20,20,235));
            g2.fillRoundRect(40,390,720,170,18,18);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.BOLD,22));
            g2.drawString("Ethan Carter",65,425);

            g2.setFont(new Font("Arial",Font.PLAIN,18));
            drawWrapped(g2,introDialogue[dialogueIndex],65,460,650);

            g2.setFont(new Font("Arial",Font.BOLD,14));
            g2.drawString("Press E",660,530);
        }

        // LETTER
        if(showingLetter){

            g2.setColor(new Color(0,0,0,170));
            g2.fillRect(0,0,getWidth(),getHeight());

            g2.setColor(new Color(243,232,205));
            g2.fillRoundRect(90,60,620,470,20,20);

            g2.setColor(Color.BLACK);

            g2.setFont(new Font("Serif",Font.BOLD,24));
            g2.drawString("Handwritten Letter",210,95);

            g2.setFont(new Font("Serif",Font.PLAIN,20));

            int y = 145;

            for(String line : letterText){
                g2.drawString(line,130,y);
                y += 40;
            }

            g2.setFont(new Font("Arial",Font.BOLD,15));
            g2.drawString("Press E",330,495);
        }

        // AFTER LETTER DIALOGUE
        if(postLetterDialogue){

            g2.setColor(new Color(0,0,0,170));
            g2.fillRect(0,0,getWidth(),getHeight());

            g2.setColor(new Color(20,20,20,235));
            g2.fillRoundRect(40,390,720,170,18,18);

            g2.setColor(Color.WHITE);

            g2.setFont(new Font("Arial",Font.BOLD,22));
            g2.drawString("Ethan Carter",65,425);

            g2.setFont(new Font("Arial",Font.PLAIN,18));
            drawWrapped(g2,afterLetter[afterIndex],65,460,650);

            g2.setFont(new Font("Arial",Font.BOLD,14));
            g2.drawString("Press E",660,530);
        }
    }

    private void drawWrapped(Graphics2D g2,String text,int x,int y,int width){

        FontMetrics fm = g2.getFontMetrics();

        String[] words = text.split(" ");

        String line = "";
        int yy = y;

        for(String w : words){

            if(fm.stringWidth(line+w) > width){

                g2.drawString(line,x,yy);
                line = w+" ";
                yy += 24;

            }else{

                line += w+" ";
            }
        }

        g2.drawString(line,x,yy);
    }

    @Override
    public void keyPressed(KeyEvent e){

        int k = e.getKeyCode();

        if(k >= 0 && k < keys.length)
            keys[k] = true;

        if(k != KeyEvent.VK_E)
            return;

        // =========================
        // INTRO DIALOGUE
        // =========================
        if(showingDialogue){

            dialogueIndex++;

            if(dialogueIndex >= introDialogue.length){

                showingDialogue = false;
                dialogueIndex = 0;
            }

            return;
        }

        // =========================
        // OPEN LETTER
        // =========================
        if(!showingLetter && nearLetter && !postLetterDialogue){

            showingLetter = true;
            return;
        }

        // =========================
        // CLOSE LETTER
        // =========================
        if(showingLetter){

            showingLetter = false;
            postLetterDialogue = true;
            return;
        }

        // =========================
        // AFTER LETTER DIALOGUE
        // =========================
        if(postLetterDialogue){

            afterIndex++;

            if(afterIndex >= afterLetter.length){

                postLetterDialogue = false;
                afterIndex = 0;

                // NOTES HAVE NOW BEEN READ
                notesRead = true;
            }

            return;
        }

        // =========================
        // EXIT SAFEHOUSE
        // ONLY AFTER READING NOTES
        // =========================
        if(nearDoor && notesRead){

            JFrame frame =
                    (JFrame)SwingUtilities.getWindowAncestor(this);

            frame.setContentPane(new GamePanel());
            frame.revalidate();
            frame.repaint();
        }
    }
    @Override
    public void keyReleased(KeyEvent e){

        int k = e.getKeyCode();

        if(k>=0 && k<keys.length)
            keys[k]=false;
    }

    @Override
    public void keyTyped(KeyEvent e){}
}