package Main;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements KeyListener, MouseListener {

    Player player;
    Enemy enemy;
    Barricade barricade;
    PoliceBoss boss;
    Keycard keycard;
    AmmoDrop ammoDrop;

    boolean bossSpawned = false;
    boolean hasKeycard = false;
    boolean nearGate = false;
    boolean bossDialogue = false;
    boolean checkpointNewspaper = false;

    // Boss reward/death handling
    boolean bossDeathPending = false;

    Rectangle gateZone = new Rectangle(1425, 235, 80, 70);

    BufferedImage background;
    BufferedImage newspaper;
    BufferedImage uiIcons;

    ArrayList<Bullet> bullets = new ArrayList<>();
    boolean[] keys = new boolean[256];
    List<Rectangle> walls = new ArrayList<>();

    int worldWidth = 1600;
    int cameraX = 0;

    boolean gameWon = false;
    boolean gameOver = false;
    boolean playerDying = false;
    int deathTimer = 0;

    // =========================
    // INTRO DIALOGUE
    // =========================

    boolean introDialogue = true;
    int dialogueIndex = 0;

    String[] intro = {
            "This... can't be Raven City.",
            "The streets are completely empty...",
            "I need to find the Chief before it's too late."
    };

    // =========================
    // FIRST ZOMBIE
    // =========================

    boolean firstZombieDialogue = false;
    boolean firstZombieTriggered = false;

    String[] zombieTalk = {
            "...What the hell is that?",
            "Stay back!"
    };

    // =========================
    // AFTER KILL
    // =========================

    boolean killDialogue = false;
    boolean killPlayed = false;

    String[] afterKill = {
            "...They're not human anymore.",
            "I have to keep moving."
    };

    int talkIndex = 0;

 // =========================
 // NEWSPAPER
 // =========================

 Rectangle newspaperZone = new Rectangle(1280, 225, 32, 32);

 boolean nearNewspaper = false;
 boolean newspaperOpened = false;
 boolean newspaperFinished = false;
 boolean newspaperReaction = false;

 int newspaperLine = 0;

 String[] newspaperText = {

     "RAVEN TIMES",
     "",
     "MYSTERIOUS ILLNESS",
     "SPREADS AT RAVEN HOSPITAL",
     "",
     "October 12",
     "",
     "Doctors report multiple",
     "patients suffering from",
     "violent behavior and unexplained infections",
     "City officials urge citizens to remain calm."
    
 };

 String[] reactionText = {

     "October 12...",

     "This happened before I was shot.",

     "I need to find the Chief."

 };
    int bossTalkIndex = 0;
    String[] bossTalk = {
        "The gate... It\'s locked.",
        "...A police officer?",
        "I\'m sorry."
    };

    public GamePanel() {
        this(120, 340);   // default spawn
    }

    public GamePanel(int spawnX, int spawnY) {

        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        ObjectiveManager.setObjective("Defeat the First Infected");

        try {
            background = ImageIO.read(getClass().getResource("/assets/residential_bg.png"));
            newspaper = ImageIO.read(getClass().getResource("/assets/newspaper.png"));
            uiIcons = ImageIO.read(getClass().getResource("/assets/ui_icons.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // OUTSIDE SPAWN
        player = new Player(spawnX, spawnY);
        player.hasGun = true;

        enemy = new Enemy(900);

        barricade = new Barricade(1590,300);
        boss = null;
        keycard = null;

        initCollision();

        Timer timer = new Timer(16, e -> {
            if (!gameWon && !gameOver)
                update();
            repaint();
        });

        timer.start();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void initCollision() {

        walls.clear();

        // ===== MAP BORDERS =====
        walls.add(new Rectangle(0, 0, 1600, 55));      // top
        walls.add(new Rectangle(0, 560, 1600, 40));    // bottom
        walls.add(new Rectangle(-20, 0, 20, 600));     // left
        walls.add(new Rectangle(1600, 0, 20, 600));    // right

        // ========= TOP HOUSES =========

        // House 1
        walls.add(new Rectangle(30, 18, 190, 135));

        // Garage 1
        walls.add(new Rectangle(240, 18, 82, 120));

        // House 2
        walls.add(new Rectangle(630, 12, 185, 138));

        // House 3
        walls.add(new Rectangle(930, 18, 175, 125));

        // ========= BOTTOM HOUSES =========

        // House 4
        walls.add(new Rectangle(35, 418, 180, 142));

        // House 5
        walls.add(new Rectangle(520, 405, 205, 155));

        // House 6
        walls.add(new Rectangle(905, 405, 195, 150));

        // ========= CARS =========

        walls.add(new Rectangle(250, 58, 56, 95));      // silver
        walls.add(new Rectangle(770, 62, 48, 92));      // red
        walls.add(new Rectangle(1088, 55, 52, 100));    // white

        walls.add(new Rectangle(245, 430, 56, 105));    // bottom left
        walls.add(new Rectangle(695, 430, 55, 105));    // bottom middle
        walls.add(new Rectangle(1095, 430, 55, 105));   // bottom right

        // ========= FENCES =========

        walls.add(new Rectangle(0, 185, 860, 12));      // upper fence
        walls.add(new Rectangle(960, 185, 200, 12));

        walls.add(new Rectangle(0, 178, 1220, 8));
        // ========= SMALL OBJECTS =========

        walls.add(new Rectangle(325, 165, 45, 25));     // trash bags
        walls.add(new Rectangle(1118, 165, 22, 22));    // barrel
    }

    public void update(){

        if(!introDialogue &&
           !firstZombieDialogue &&
           !killDialogue &&
           !newspaperOpened &&
           !newspaperReaction &&
           !bossDialogue){

            
            int oldX = player.x;
            int oldY = player.y;

            player.update(keys);

            for(Rectangle wall : walls){

                if(player.getBounds().intersects(wall)){
                    player.x = oldX;
                    player.y = oldY;
                    break;
                }
            }
        }

        // Trigger first zombie

        if(!firstZombieTriggered && player.x>=330){

            firstZombieTriggered = true;
            firstZombieDialogue = true;
            talkIndex = 0;
        }

        // Newspaper

        nearNewspaper = killPlayed && player.getBounds().intersects(newspaperZone);

        // Spawn Police Boss
        if(newspaperFinished && !bossSpawned && player.x >= 1360){

            bossSpawned = true;
            bossDialogue = true;
            bossTalkIndex = 0;
            boss = new PoliceBoss(1480,280);
            bossDeathPending = false;

            // Boss scream before the dialogue starts
            SoundManager.play("boss_scream_intro.wav");
        }

        // Zombie

        if(enemy!=null &&
           !introDialogue &&
           !firstZombieDialogue &&
           !killDialogue &&
           !newspaperOpened &&
           !newspaperReaction){

            enemy.update(player);

            if(enemy.canAttack(player))
                player.takeDamage(10);

            if(enemy.isDead()){

                SoundManager.play("zombie_death.wav");
                enemy = null;

                if(!killPlayed){

                    killPlayed = true;
                    killDialogue = true;
                    ObjectiveManager.setObjective("Investigate the Newspaper");
                    talkIndex = 0;
                }
            }
        }


        if(boss!=null && !bossDialogue){

            boss.update(player);

            if(boss.canAttack(player)){
                SoundManager.play("boss_heavy_attack.wav");
                player.takeDamage(20);
            }
        }

        // Finish the boss death animation before dropping rewards
        if(boss != null && boss.isDead() && bossDeathPending){

            keycard = new Keycard(boss.x + 6, boss.y + 4);
            ammoDrop = new AmmoDrop(boss.x + 28, boss.y + 10, 24);

            boss = null;
            bossDeathPending = false;

            ObjectiveManager.setObjective("Collect the Keycard");
        }

        // Bullets

        for(int i=0;i<bullets.size();i++){

            Bullet b = bullets.get(i);

            b.update();

            if(enemy!=null &&
               b.getBounds().intersects(enemy.getBounds())){

                enemy.takeDamage();
                b.alive=false;
            }

            if (boss != null && b.getBounds().intersects(boss.getBounds())) {

                boss.takeDamage();
                b.alive = false;

                if (boss.getHealth() <= 0 && !bossDeathPending) {

                    bossDeathPending = true;
                    SoundManager.play("boss_death.wav");
                }
            }

            if(!b.alive){

                bullets.remove(i);
                i--;
            }
        }


        if (keycard != null &&
        	    !keycard.isCollected() &&
        	    player.getBounds().intersects(keycard.getBounds())) {

        	    keycard.collect();
        	    hasKeycard = true;

        	    ObjectiveManager.setObjective("Unlock the Barricade");
        	}

        	if (ammoDrop != null &&
        	    !ammoDrop.isCollected() &&
        	    player.getBounds().intersects(ammoDrop.getBounds())) {

        	    ammoDrop.collect(player);
        	}

        nearGate = player.getBounds().intersects(gateZone);

        if(player.isDead() && !playerDying){
            playerDying = true;
            deathTimer = 0;
        }

        if(playerDying){
            deathTimer++;

            // 6 frames × 10 ticks = 60
            if(deathTimer >= 60){
                gameOver = true;
                playerDying = false;
            }
        }
        if(player.x<0)
            player.x=0;

        if(player.x>worldWidth-player.width)
            player.x=worldWidth-player.width;

        cameraX=player.x-400;

        if(cameraX<0)
            cameraX=0;

        if(cameraX>worldWidth-800)
            cameraX=worldWidth-800;

        // Reaching the gate does not complete the chapter by itself.
        // The player must have the keycard and explicitly press E at the gate.
    }
    private void respawn(){
    	playerDying = false;
    	deathTimer = 0;
        gameOver = false;

       
        bullets.clear();

        if(checkpointNewspaper){

            // Respawn after newspaper
            player.respawn(120, 340);

            killPlayed = true;
            newspaperFinished = true;
            newspaperOpened = false;
            newspaperReaction = false;

            firstZombieTriggered = true;
            firstZombieDialogue = false;
            killDialogue = false;

            ObjectiveManager.setObjective("Find a way past the Barricade");

            enemy = null;

            bossSpawned = true;
            boss = new PoliceBoss(1480,280);
            bossDeathPending = false;
            keycard = null;

            // Boss returns after checkpoint respawn
            SoundManager.play("boss_scream_respawn.wav");
            hasKeycard = false;

        }else{

            // Respawn before first zombie
            player.respawn(120, 340);

            firstZombieTriggered = false;
            firstZombieDialogue = false;

            killPlayed = false;
            killDialogue = false;

            newspaperFinished = false;
            newspaperOpened = false;
            newspaperReaction = false;

            bossSpawned = false;
            boss = null;
            bossDeathPending = false;
            keycard = null;
            hasKeycard = false;

            enemy = new Enemy(900);

            ObjectiveManager.setObjective("Defeat the First Infected");
        }
    }
    // =========================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Background
        if (background != null)
     
        	g2.drawImage(background, -cameraX, 0, null);

        // World
        g2.translate(-cameraX, 0);

        barricade.draw(g2);
     // Newspaper sprite
        if (killPlayed && !newspaperFinished && newspaper != null) {
            g2.drawImage(newspaper, 1280, 225, 32, 32, null);
        }

        if (enemy != null) enemy.draw(g2);
        if (boss != null) boss.draw(g2);
        if (keycard != null) keycard.draw(g2);
        if (ammoDrop != null)
            ammoDrop.draw(g2);

        for (Bullet b : bullets)
            b.draw(g2);

        player.draw(g2);   // ← this is where ethan_gun_sheet is used
    

     
        // Newspaper prompt
        if (killPlayed &&
            nearNewspaper &&
            !newspaperFinished &&
            !newspaperOpened &&
            !newspaperReaction &&
            !introDialogue &&
            !firstZombieDialogue &&
            !killDialogue) {

            g2.setColor(new Color(255,255,255,230));
            g2.fillRoundRect(1245,185,120,28,8,8);

            g2.setColor(Color.BLACK);
            g2.drawString("E - Read",1273,203);
        }


        if(nearGate && hasKeycard){

            g2.setColor(new Color(255,255,255,230));
            g2.fillRoundRect(1390,180,120,28,8,8);

            g2.setColor(Color.BLACK);
            g2.drawString("E Unlock",1412,198);
        }

        // Return to screen coordinates
        g2.translate(cameraX, 0);

        drawHUD(g2);

        if (introDialogue)
            drawDialogue(g2, intro[dialogueIndex]);

        if (firstZombieDialogue)
        	
            drawDialogue(g2, zombieTalk[talkIndex]);

        if (killDialogue)
            drawDialogue(g2, afterKill[talkIndex]);

        if(bossDialogue)
            drawDialogue(g2,bossTalk[bossTalkIndex]);

        if (newspaperOpened) {
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0,0,800,600);

            g2.setColor(new Color(243,232,205));
            g2.fillRoundRect(90,60,620,470,20,20);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Serif",Font.BOLD,24));
            g2.drawString("RAVEN TIMES",140,95);

            g2.setFont(new Font("Serif",Font.PLAIN,18));

            int yy = 140;
            for (int i = 0; i <= newspaperLine; i++) {
                g2.drawString(newspaperText[i],125,yy);
                yy += 30;
            }

            g2.setFont(new Font("Arial",Font.BOLD,14));
            g2.drawString("Press E",330,495);
        }

        if (newspaperReaction)
            drawDialogue(g2, reactionText[newspaperLine]);

        if (gameWon) {
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0,0,800,600);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.BOLD,28));
            g2.drawString("CHAPTER 1 COMPLETE",175,250);
            g2.setFont(new Font("Arial",Font.PLAIN,18));
            g2.drawString("Next: Downtown District",250,290);
        }

        if (gameOver) {

            g2.setColor(new Color(0,0,0,200));
            g2.fillRect(0,0,800,600);

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial",Font.BOLD,42));
            g2.drawString("GAME OVER",210,245);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.PLAIN,18));
            g2.drawString("Press R to Restart from Checkpoint",180,290);
        }}
    // =========================

    private void drawHUD(Graphics2D g2){

        g2.setColor(new Color(0,0,0,180));
        g2.fillRoundRect(15,15,250,65,12,12);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial",Font.BOLD,12));
        g2.drawString("HP",25,30);

        g2.setColor(Color.GRAY);
        g2.fillRect(55,20,150,12);

        g2.setColor(Color.RED);
        g2.fillRect(55,20,player.health*150/100,12);

        g2.setColor(Color.WHITE);
        g2.drawRect(55,20,150,12);

        g2.setFont(new Font("Arial",Font.PLAIN,11));
        g2.drawString("Objective",25,47);
        g2.drawString(ObjectiveManager.getObjective(),25,62);
        g2.drawImage(
        	    uiIcons,
        	    18, 58, 34, 74,          // destination
        	    121, 23, 273, 767,       // source
        	    null
        	);
        
        // Ammo text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString(
        	    String.format("%02d / %02d", player.ammo, player.reserveAmmo),
        	    52, 74
        	);
    }

    private void drawDialogue(Graphics2D g2,String text){

        g2.setColor(new Color(0,0,0,170));
        g2.fillRect(0,0,800,600);

        g2.setColor(new Color(22,22,22,235));
        g2.fillRoundRect(40,390,720,170,18,18);

        g2.setColor(Color.WHITE);

        g2.setFont(new Font("Arial",Font.BOLD,22));
        g2.drawString("Ethan Carter",65,425);

        g2.setFont(new Font("Arial",Font.PLAIN,18));
        g2.drawString(text,65,465);

        g2.setFont(new Font("Arial",Font.BOLD,14));
        g2.drawString("Press E",665,530);
    }

    // =========================
    // KEYBOARD
    // =========================

    @Override
    public void keyPressed(KeyEvent e){

    	int k = e.getKeyCode();
    	
    	if(gameOver && k == KeyEvent.VK_R){
    	    respawn();
    	    return;
    	}

    	// Ignore key repeat while the key is held
    	if(k >= 0 && k < keys.length){
    	    if(keys[k]) return;
    	    keys[k] = true;
    	}
    	if(playerDying)
    	    return;

     // Reload
        if(k == KeyEvent.VK_R){
            player.reload();
            return;
        }
        // Intro

        if(introDialogue){

            dialogueIndex++;

            if(dialogueIndex>=intro.length){

                introDialogue=false;
                dialogueIndex=0;
            }
            return;
        }

        // First zombie

        if(firstZombieDialogue){

            talkIndex++;

            if(talkIndex>=zombieTalk.length){

                firstZombieDialogue=false;
                talkIndex=0;
            }
            return;
        }

        // After kill

        if(killDialogue){

            talkIndex++;

            if(talkIndex>=afterKill.length){

                killDialogue=false;
                talkIndex=0;
            }
            return;
        }

        if(bossDialogue){
            bossTalkIndex++;
            if(bossTalkIndex>=bossTalk.length){
                bossDialogue=false;
                bossTalkIndex=0;
            }
            return;
        }

        // Open newspaper

        if(killPlayed &&
           nearNewspaper &&
           !newspaperFinished &&
           !newspaperOpened &&
           !newspaperReaction){

            newspaperOpened=true;
            newspaperLine=0;
            return;
        }

        // Reading newspaper

        if(newspaperOpened){

            newspaperLine++;

            if(newspaperLine>=newspaperText.length){

                newspaperOpened=false;
                newspaperReaction=true;
                
                newspaperLine=0;
            }
            return;
        }

        // Reaction

        if(newspaperReaction){

            newspaperLine++;

            if(newspaperLine>=reactionText.length){

            	newspaperReaction = false;
            	newspaperFinished = true;
            	checkpointNewspaper = true;

            	ObjectiveManager.setObjective("Find a way past the Barricade");
            	newspaperLine = 0;
            }
        }

        if(k==KeyEvent.VK_E && nearGate && hasKeycard){
            SoundManager.play("gate_open.wav");
            gameWon=true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e){

        int k=e.getKeyCode();

        if(k>=0 && k<keys.length)
            keys[k]=false;
    }

    @Override
    public void keyTyped(KeyEvent e){}

    // =========================
    // SHOOTING
    // =========================

    @Override
    public void mousePressed(MouseEvent e){

    	if(introDialogue ||
    			   firstZombieDialogue ||
    			   killDialogue ||
    			   newspaperOpened ||
    			   newspaperReaction ||
    			   gameWon ||
    			   gameOver ||
    			   playerDying)
    			    return;;

        // Don't fire if no ammo
        if(!player.shoot())
            return;

        int dx = 0;
        int dy = 0;

        switch(player.getDirection()){

            case 0 -> dy = 1;
            case 1 -> dx = -1;
            case 2 -> dx = 1;
            case 3 -> dy = -1;
        }

        int bulletX = player.x + 32;
        int bulletY = player.y + 32;

        switch (player.getDirection()) {

            case 0: // DOWN
                bulletX = player.x + 28;
                bulletY = player.y + 54;
                break;

            case 1: // LEFT
                bulletX = player.x + 8;
                bulletY = player.y + 25;
                break;

            case 2: // RIGHT
                bulletX = player.x + 56;
                bulletY = player.y + 25;
                break;

            case 3: // UP
                bulletX = player.x + 32;
                bulletY = player.y + 9;
                break;
        }

        bullets.add(new Bullet(
            bulletX,
            bulletY,
            dx,
            dy
        ));
    }
    @Override public void mouseReleased(MouseEvent e){}
    @Override public void mouseClicked(MouseEvent e){}
    @Override public void mouseEntered(MouseEvent e){}
    @Override public void mouseExited(MouseEvent e){}
}