package game;
import javax.imageio.*;
import java.io.*;
import java.awt.image.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.*;

class UseInConstants {
    /**
     * Hight and width of the Game Frame
     */
    final static int WIDTH=305;
    final static int HEIGHT=450;
    /*In Simple Mode Puyo pair will be generated randomly*/
    final static int SimpleMode=0;
    /* In AI mode next puyo pair will be dtermined based on the current state of 
     * the game.
     */
    final static int AIMode=1;
    /* Indicate current game mode    */
    static int GameMode=SimpleMode;
    
    /**
     * single sphere Size
     */
    final static int FaceSize=25;
    /**
     * Starting Position of new face pair
     */
    final static int StartPos=185;
    /**
     * Delay between two consequtive drop down
     *and its value decreases as level number increase
     */
    static int TIMERDELAY=500;        
    /**
     * Whole screen is divided into 2D grid
     */
    /**
     * Number of columns in 2D grid
     */
    final static int NoofHBox=WIDTH/FaceSize;
    /**
     * Number of rows in 2D grid
     */
    final static int NoofVBox=HEIGHT/FaceSize;
    /**
     *Total grids
     */
    final static int TotalBox=NoofHBox*NoofVBox-10;
    /**
     *BondSize  that is eligible to elimination
     */
    static int BondSize=1;
}

public class PuyoGame extends javax.swing.JFrame {
    /**
     * store state of 2d grid as 2d array
     */
    private static int[] GameFillArr=new int[UseInConstants.TotalBox];
    /**
     * keyboard listener
     */
    private static KeyAdapter klistener;
    /**
     * graphics context for Frame window
     */
    private Graphics g2;
    /**
     * variables keeps score and level
     */
    private static int gameScoreValue=0,gamelevelValue=1,returnIndex;
    /**
     *game panel that display grid
     */
    private static GameController gameController;
       
    /**
     *panel displaying at southmost side
     */
    private static ProgressBarPanel progressBarPanel;
    /**
     *ask whether to quit or not
     */
    private QuitPanel quitPanel;
    /**
     *window event listener
     */
    private GameWindowEventListener gameWindowEventListener;
    /**
     *button controls in use
     */
    private JButton jButton1,jButton2;
    /**
     * coordinate of a 2D position
     */
    private Point p;
    /**
     * image array contains differenet colored sphere
     */
    private static Image[] image;
    /**
     * panel to display current score
     */
    private static CurrentScorePanel currentScorePanel;
    /**
     * variables in use  
     */
    private char currentBox=195,siblingBox=194,previousBox;
    /**
     * Object representing currently generated spheres
     */
    private static PairFace pf;
    /**
     *Object representing next coming spheres
     */
    private static PairFace nextPf;
    /**
     * Thread of control.
     */
    private static Thread gameControl,timer;
    /**
     * paused and windowIconofied are binary semaphore
     * semaphore variables controls game pause and start
     */
    static boolean paused=true;
    static boolean windowIconofied=false;
    /**
     *integer variable store number returned by random function
     */
    private int randomValue;
    /**
     * Constructor
     */
    
    public PuyoGame(){
        super("Puyo Demo Game");
        initComponents();
    }
    
    /**
     * perform initialization operation
     */
    private void initComponents(){
        addKeyListener((klistener=new KeyController()));
        gameWindowEventListener=new GameWindowEventListener();
        addWindowListener(gameWindowEventListener);
        setGameFillArr();
        setState(JFrame.NORMAL);
        add(addCloseButton());
        add(addMinimizeButton());
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        gameController=new GameController(new TimerListener());
        gameController.setBounds(200,100, UseInConstants.WIDTH,UseInConstants.HEIGHT);
        setBackground(Color.white);
        setForeground(new java.awt.Color(204, 0, 255));
        setIconImage(PuyoGame.image[2]);
        setBounds(200,100, UseInConstants.WIDTH,UseInConstants.HEIGHT);
        getContentPane().add(gameController);
        
        
    }
    
    /**
     * return GameController object
     */
    public static GameController getGameController(){
        return gameController;
    }
    /**
     * return object representing state of Grid
     */
    public static int[] getGameFillArr(){
        return GameFillArr;
    }
    /**
     * return image array object containing different colored faces
     */
    public static Image[] getImageArray(){
        return image;
    }
    /**
     * return current game score
     */
    public static int getGameScoreValue(){
        return gameScoreValue;
    }
    /**
     * return current game level
     */
    public static int getGamelevelValue(){
        return gamelevelValue;
    }
    /**
     * set game score to specified value
     */
    public static void setGameScoreValue(int val){
        gameScoreValue=val;
    }
    /**
     * set game level to specified value
     */
    public static void setGamelevelValue(int val){
        gamelevelValue=val;
    }
    /**
     * return object representing current score panel
     */
    public static CurrentScorePanel getCurrentScorePanel(){
        return currentScorePanel;
    }
    /**
     * return current pair face
     */
    public static PairFace getCurrentPairFace(){
        return pf;
    }
    /**
     * return object representing close button
     */
    private JButton addCloseButton(){
        jButton1=new JButton();
        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon("image/switch.jpg"));
        
        
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        
        jButton1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jButton1.setBounds(283, 5, 20, 18);
        return jButton1;
    }
    /**
     * return object representing close button
     */
    private JButton addMinimizeButton(){
        jButton2=new JButton();
        jButton2.setBackground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon("image/compensate.jpg"));
        //jButton2.setToolTipText("Click To Hide");
        
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jButton2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jButton2.setBounds(263, 5, 20, 18);
        return jButton2;
    }
    /**
     * called when close clicked by user and display quit panel on screen
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        
        quitPanel.setBounds(50,60,200,80);
        paused=true;
        gameController.add(quitPanel);
        
    }
    /**
     * called when minimize button pressed and sets windowIconofied semaphore
     */
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        
        setState(JFrame.ICONIFIED);
        windowIconofied=true;
        
        
    }
    /**
     * set empty grid state
     */
    public static void setGameFillArr(){
        for(int i=0;i<GameFillArr.length;i++){
            GameFillArr[i]=-1;
        }
    }
    
    /**
     * generate random number between 0 to 3
     */
    private int random(){
        
        randomValue=(int)(Math.random()*10);
        return randomValue%4;
    }
    /**
     * create object representing ProgressBar Panel
     */
    private JPanel createProgressBar(){
        progressBarPanel = new ProgressBarPanel();
        progressBarPanel.setBounds(5,UseInConstants.HEIGHT-35,UseInConstants.WIDTH-15,30 );
        progressBarPanel.setFocusable(false);
        return progressBarPanel;
    }
    /**
     * paint screen by white color
     */
    private void clearScreen(){
        
        g2.setColor(Color.white);
        for(int  i=0;i<UseInConstants.TotalBox;i++) {
            p=getPoint(i);
            g2.fillRect(p.x,p.y,p.x+UseInConstants.FaceSize,p.y+UseInConstants.FaceSize);
        }
        
    }
    /**
     * This method checks whether index position is filled or not. if index position
     * is filled it fills the grid index+UseInConstants.NoofHBox by faceType value
     */
    private int CheckFill(int index,int faceType,int KeyType){
        
        if(index<0){
            
            GameFillArr[index+UseInConstants.NoofHBox]=faceType;
            returnIndex=index+UseInConstants.NoofHBox;
            return returnIndex;
        } else if(GameFillArr[index]!=-1){
            
            GameFillArr[index+UseInConstants.NoofHBox]=faceType;
            returnIndex=index+UseInConstants.NoofHBox;
            return returnIndex;
        } else{
            //System.out.println("CheckFill3:"+index);
            returnIndex=index;
            return returnIndex;
        }
    }
    /**
     * Return the coordinate of top left corner of grid with specified number.
     */
    public static Point getPoint(int boxno){
        int xbox=UseInConstants.NoofHBox;
        int ybox=UseInConstants.NoofVBox;
        int y;
        if(boxno/xbox!=0)
            y=UseInConstants.HEIGHT-(boxno/xbox)*UseInConstants.FaceSize-(int)(UseInConstants.FaceSize*2.5);
        else
            y=UseInConstants.HEIGHT-(int)(UseInConstants.FaceSize*2.5);
        int x=(boxno%xbox)*UseInConstants.FaceSize;
        
        return new Point(x,y);
    }
    /**
     * Fills the screen accord to state in GameFillArr array.
     */
    public void drawFaces(){
        
        for(int i=0;i<UseInConstants.TotalBox;i++) {
            if(GameFillArr[i]!=-1){
                
                g2.drawImage(image[GameFillArr[i]],getPoint(i).x,getPoint(i).y,UseInConstants.FaceSize,UseInConstants.FaceSize,null);
            }
        }
    }
    /**
     * Entry point function
     */
    public static void main(String args[]){
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PuyoGame().setVisible(true);
            }
        });
    }
    
//-----------------------------------------------------------//
    /**
     * ClassName: DemoPanel
     * Description : load images of different colored faces
     */
    
    class DemoPanel extends JPanel{
        Point p;
        /**
         * object Constructor
         */
        DemoPanel(){
            loadImage();
            add(createProgressBar());
            setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 5,false));
            setLayout(null);
        }
         
        /**
         *  loadImages for different colored faces
         */
        private Image[] loadImage(){
            image=new Image[4];
            try{
                image[0]=ImageIO.read(new File("image/puyo_red.jpg"));
                image[1]=ImageIO.read(new File("image/puyo_yellow.jpg"));
                image[2]=ImageIO.read(new File("image/puyo_green.jpg"));
                image[3]=ImageIO.read(new File("image/puyo_blue.jpg"));
                ScorePanel.image=ImageIO.read(new File("image/switch.jpg"));
            }catch(Exception e){e.printStackTrace();}
            return image;
        }
    }
    
    /**
     * ClassName - GamePanel
     * Description -- Listener of timer event
     */
    
    static int timeCounter;
    class TimerListener {
        int nextBox;
        int timeCounter=0;
        public int actionPerformed(){
            int getx=pf.getX(),gety=pf.getY();
            if(GameFillArr[pf.getX()]!=-1&&GameFillArr[pf.getY()]!=-1) {
                synchronized(klistener){
                    if(getx<(UseInConstants.TotalBox-UseInConstants.NoofHBox*4)&&gety<(UseInConstants.TotalBox-UseInConstants.NoofHBox*4)){
                        if(GameFillArr[getx]!=-1&&GameFillArr[gety]!=-1){
                            klistener.notifyAll();
                            timeCounter++;
                            if(timeCounter%20==0&&timeCounter>=20)
                                System.gc();
                        }
                    } else{
                        try{
                            gameControl.interrupt();
                        }catch(Exception e){e.printStackTrace();}
                        return -1;
                        
                    }
                }
            }
            pf.setY(CheckFill(pf.getY()-UseInConstants.NoofHBox , pf.faceY,nextBox));
            pf.setX(CheckFill(pf.getX()-UseInConstants.NoofHBox , pf.faceX,nextBox));
            return 0;
        }
        
    }
    /**
     * Class Name:GameController
     * Functionality:GameController object is responsible for painting,removing and redrawing of
     * sphere during game run time.
     */
    
    
    public class GameController extends DemoPanel implements Runnable{
        
        /**
         * integer variables store coordinate of sphere current position
         */
        private int xFace,yFace,xFace_x,xFace_y,yFace_x,yFace_y;
        /**
         * Panel object to display final score after game over
         */
        private ScorePanel scorePanel;
        /**
         * Panel to display game over message
         */
        private GameOverPanel gameOverPanel;
        /**
         * object that listen timing event
         */
        private TimerListener tlistener;
        /**
         * Reference graphics object associated with game screen
         */
        private Graphics screenGraphics;
        /**
         * return score panel  associated with game controller panel
         */
        public ScorePanel getScorePanel(){
            return scorePanel;
        }
        /**
         * return object representing gameover panel
         */
        public GameOverPanel getGameOverPanel(){
            return gameOverPanel;
        }
        /**
         *return graphics object to paint mainscreen
         */
        public Graphics getScreenGraphics(){
            return screenGraphics;
        }
        /**
         *GameController object constructor
         */
        GameController(TimerListener tl){
            
            initVector_Stack();
            setFocusable(true);
            addKeyListener((klistener=new KeyController()));
            scorePanel=new ScorePanel(this);
            quitPanel=new QuitPanel(this);
            gameOverPanel=new GameOverPanel(this,scorePanel);
            currentScorePanel=new CurrentScorePanel(this,getGraphics());
            currentScorePanel.setBounds(0,0,308,65);
            currentScorePanel.setFocusable(true);
            currentScorePanel.setBackground(Color.white);
            currentScorePanel.setOpaque(true);
            add(currentScorePanel);
            gameControl=new Thread(this);
            gameControl.start();
            tlistener=tl;
            
            
            try{
                Thread.sleep(100);}catch(InterruptedException e){e.printStackTrace();}
            /**
             * Anonymous inner class to create timer thread
             */
            timer=new Thread(new Runnable(){
                public void run(){
                    whileLable:
                        while(true){
                            try{
                                synchronized(progressBarPanel){
                                    while(paused){
                                        progressBarPanel.wait();
                                        
                                    }
                                }
                                synchronized(gameWindowEventListener){
                                    while(windowIconofied){
                                        gameWindowEventListener.wait();
                                        
                                    }
                                }
                                gameController.requestFocus();
                                currentScorePanel.setFreeMemory(Runtime.getRuntime().freeMemory());
                                currentScorePanel.setTotalMemory(Runtime.getRuntime().totalMemory());
                                if(tlistener.actionPerformed()==-1){
                                    scorePanel.setScore(gameScoreValue,gamelevelValue,100);
                                    gameOverPanel.setBounds(80,160,165,40);
                                    progressBarPanel.getStartButton().setEnabled(false);
                                    add(gameOverPanel,scorePanel);
                                    synchronized(progressBarPanel) {
                                        progressBarPanel.wait();
                                    }
                                }
                                Thread.sleep(UseInConstants.TIMERDELAY);
                                
                            }catch(InterruptedException ie) {
                                ie.printStackTrace();
                                
                                
                                
                                
                            } catch(Exception e){e.printStackTrace();}
                        }
                        
                }
            });
            timer.start();
            
        }
        
        /**
         * life cycle method for gamecontrol thread
         */
        
        public void run(){
            synchronized(klistener){
                whileLabel:
                    while(true){
                        if(UseInConstants.GameMode==UseInConstants.SimpleMode) {
                            if(nextPf==null){
                                pf=PairFace.createNewPairFace();
                            } else
                                pf=nextPf;
                        }
                        if(UseInConstants.GameMode==UseInConstants.SimpleMode)
                            nextPf=PairFace.createNewPairFace();
                        if(UseInConstants.GameMode==UseInConstants.AIMode)
                            pf=PairFace.createPairFaceUsingAI();
                        
                        
                        if(currentScorePanel!=null)
                            currentScorePanel.setNextPair(nextPf);
                        initVariable();
                        try{
                            klistener.wait(); // waits till current sphere dropping down
                            processGame(pf.getX(),pf.getY());
                            processGame(pf.getX(),pf.getY());
                        } catch(InterruptedException e){
                        }
                    }
            }
        }
        /**
         * prints contents of GameFillArr grid
         */
        private void printGameFillArr(){
            //System.out.println("GameFillArr:\n");
            for(int i=0;i< UseInConstants.TotalBox ;i++ ){
                if(i%UseInConstants.NoofHBox==0)
                    System.out.println();
                System.out.print(" "+GameFillArr[i]);
                
            }
        }
        /**
         * initialize co-ordinate variables
         */
        private void initVariable(){
            xFace=pf.getX();
            xFace_x=getPoint(xFace).x;
            xFace_y=getPoint(xFace).y;
            yFace=pf.getY();
            yFace_x=getPoint(yFace).x;
            yFace_y=getPoint(yFace).y;
        }
        /**
         * integer array store values in LIFO order
         */
        int[] stack=new int[UseInConstants.TotalBox];
        /**
         * integer array store position of chain of grid of maximum length having
         * same colored faces. if length of this chain is equal to UseInConstants.BondSize then
         * this chain will be removed and remained empty grids willbe filled by dropping upper
         *sphere by one grid.
         */
        int[] sameFaceVector=new int[UseInConstants.TotalBox];
        /**
         * variable storing Stack Top
         */
        int stackTop=-1;
        /**
         * contains number of values stored in sameFaceVector array
         */
        int vectorCount=0;
        /**
         * initialize stack and sameFaceVector array by -1 meaning empty position
         */
        void initVector_Stack() {
            for(int i=0;i<stack.length;i++)
                stack[i]=-1;
            for(int i=0;i<sameFaceVector.length;i++)
                sameFaceVector[i]=-1;
        }
        /**
         * print values of specified integer array at system.out
         */
        void printIntArr(int[] arr){
            for(int i=0;i<arr.length;i++){
                System.out.print(" "+arr[i]);
            }
        }
        /**
         * Perform pop operation on stack array.
         */
        int stack_pop(){
            int val=stack[0];
            for(int i=0;i<stack.length-1;i++){
                stack[i]=stack[i+1];
            }
            stack[stack.length-1]=-1;
            return val;
        }
        /**
         * processGame process grid state and search for chain of max length having
         * grid with same color sphere.
         *
         */
        private void processGame(int getx,int gety) {
            
            for(int i=0;i<UseInConstants.TotalBox;i++)
                if(GameFillArr[i]!=-1){
                evaluator(i);
                
                if(vectorCount>=UseInConstants.BondSize){
                    gameScoreValue+=vectorCount*2;
                    if(((int)(gameScoreValue/50))!=0 ){
                        gamelevelValue +=1;
                        if(UseInConstants.TIMERDELAY>300)
                            UseInConstants.TIMERDELAY-=(int)(gameScoreValue-gamelevelValue*2);
                        else if(UseInConstants.TIMERDELAY>200)
                            UseInConstants.TIMERDELAY-=(int)(gameScoreValue-gamelevelValue/2);
                        else if(UseInConstants.TIMERDELAY>100)
                            UseInConstants.TIMERDELAY-=(int)(gameScoreValue-gamelevelValue/4);
                        else if(UseInConstants.TIMERDELAY>50)
                            UseInConstants.TIMERDELAY-=(int)(gameScoreValue-gamelevelValue/6);
                        else{}
                        gameScoreValue=0;
                        
                    }
                    progressBarPanel.setProgressBar(gameScoreValue);
                    currentScorePanel.resetScore(gameScoreValue,gamelevelValue);
                    removeFaces();
                }
                
                vectorCount=0;stackTop=-1;
                initVector_Stack();
                
                }
        }
        /**
         * Eliminate chain of spheres of length equal or greater than UseInConstants.FaceSize
         * and adjust resulting emptied grids.
         */
        private void removeFaces() {
            for(int i=0;i<vectorCount;i++) {
                p=getPoint(sameFaceVector[i]);
                g2.setColor(Color.white);
                g2.fillRect(p.x,p.y,p.x+UseInConstants.FaceSize,p.y+UseInConstants.FaceSize);
                GameFillArr[sameFaceVector[i]]=-1;
                
            }
            adjustFaces();
            clearScreen();
            drawFaces();
        }
        /**
         * Checks column number i and returns belowest filled grid number in column i.
         */
        private int checkVGrid(int i) {
            for(;i<UseInConstants.TotalBox ;i+=UseInConstants.NoofHBox)
                if(GameFillArr[i]!=-1)
                    return i;
            return -1;
        }
        /**
         * Adjust resulting emptied grid and fill them by sphere next above to them
         */
        private void adjustFaces() {
            int upperFace;
            for(int j=0;j<UseInConstants.NoofHBox;j++)
                for(int i=j;i<UseInConstants.TotalBox-(UseInConstants.NoofHBox-j);i+=UseInConstants.NoofHBox) {
                if(GameFillArr[i]==-1&&(upperFace=checkVGrid(i))!=-1) {
                    GameFillArr[i]=GameFillArr[upperFace];
                    GameFillArr[upperFace]=-1;
                    try{
                        
                    }catch(Exception e){e.printStackTrace();}
                    drawFaces();
                }
                
                }
        }
        /**
         * perform searching operation on integer array.
         */
        private int searchArray(int[] arr,int key){
            for(int i=0;i<arr.length;i++){
                if(arr[i]==key)
                    return i;
            }
            return -1;
        }
        /**
         * Find sphere chain of maximum length
         */
        
       public void evaluator(int i){
            if(i>=UseInConstants.TotalBox)
                return;
            sameFaceVector[vectorCount++]=i;
            while(true){
                if(searchArray(sameFaceVector,i+1)==-1&&GameFillArr[i+1]==GameFillArr[i]){
                    sameFaceVector[vectorCount++]=i+1;
                    stack[++stackTop]=i+1;
                }
                if((i-1)>=0&&searchArray(sameFaceVector,i-1)==-1&&GameFillArr[i-1]==GameFillArr[i]) { //  System.out.println("\n2");
                    sameFaceVector[vectorCount++]=i-1;
                    stack[++stackTop]=i-1;
                }
                if(searchArray(sameFaceVector,i+UseInConstants.NoofHBox)==-1&&GameFillArr[i+UseInConstants.NoofHBox]==GameFillArr[i]) { //  System.out.println("i="+i+"\n3");
                    sameFaceVector[vectorCount++]=(i+UseInConstants.NoofHBox);
                    stack[++stackTop]=(i+UseInConstants.NoofHBox);
                }
                if((i-UseInConstants.NoofHBox)>=0 &&searchArray(sameFaceVector,i-UseInConstants.NoofHBox)==-1 && GameFillArr[i-UseInConstants.NoofHBox]==GameFillArr[i]){
                    sameFaceVector[vectorCount++]=(i-UseInConstants.NoofHBox);
                    stack[++stackTop]=(i-UseInConstants.NoofHBox);
                }
                if(stackTop>=0 && stack[stackTop]!=-1) {
                    i=stack_pop();
                    stackTop--;
                    
                    
                } else break;
                
                
            }
        }
        /**
         * Called by java platform GUI manager during painting
         */
        public void paintComponent(Graphics g){
            screenGraphics=g;
            g2=g;
            clearScreen();
            g2.setColor(Color.white);
            if(GameFillArr[xFace]==-1)
                g2.fillRect(xFace_x,xFace_y,xFace_x+ UseInConstants.FaceSize,yFace_y+UseInConstants.FaceSize);
            if(GameFillArr[yFace]==-1)
                g2.fillRect(yFace_x,yFace_y,yFace_x+ UseInConstants.FaceSize,yFace_y+UseInConstants.FaceSize);
            drawFaces();
            initVariable();
            setFont(new java.awt.Font("Verdana", Font.PLAIN, 20));
            g2.drawImage(image[pf.getFaceX()],xFace_x,xFace_y,UseInConstants.FaceSize,UseInConstants.FaceSize,null);
            g2.drawImage(image[pf.getFaceY()],yFace_x,yFace_y,UseInConstants.FaceSize,UseInConstants.FaceSize,null);
        }
        
        /**
         * clear current sphere pair grids
         */
        public void clearImage(Graphics g){
            
            g.setColor(Color.white);
            g.fillRect(xFace_x,xFace_y,xFace_x+UseInConstants.FaceSize,yFace_y+UseInConstants.FaceSize);
            g.fillRect(yFace_x,yFace_y,yFace_x+UseInConstants.FaceSize,yFace_y+UseInConstants.FaceSize);
        }
        
    }
    
    
    
//------------------------------------------------
    /**
     * Listener for Mouse events.
     */
    class MouseController extends MouseAdapter{
        public void MouseClicked(MouseEvent me){}
    }
    
    
//-------------------------------------------------
    /**
     * Listener of keyboard events.
     */
    class KeyController extends KeyAdapter{
        int w=getWidth()/25;
        
        public void keyPressed(KeyEvent ke){
           if(PuyoGame.paused==false){
            switch(ke.getKeyCode()){
                
                case KeyEvent.VK_DOWN:
                    pf.setY(CheckFill(pf.getY()-UseInConstants.NoofHBox , pf.faceY,KeyEvent.VK_DOWN));
                    pf.setX(CheckFill(pf.getX()-UseInConstants.NoofHBox , pf.faceX,KeyEvent.VK_DOWN));
                    break;
                    
                case KeyEvent.VK_UP:
                    if(GameFillArr[pf.getX()]==-1){
                        pf.rotate(getGameController().getScreenGraphics());}
                    break;
                    
                case KeyEvent.VK_LEFT:
                    if(pf.getX()%UseInConstants.NoofHBox!=0 ){
                        if(GameFillArr[pf.getX()-1]==-1&&GameFillArr[pf.getY()-1]==-1){
                            if(GameFillArr[pf.getX()]==-1&&GameFillArr[pf.getY()]==-1)  {
                                pf.setX(pf.getX()-1);
                                pf.setY(pf.getY()-1);} }
                    }
                    break;
                    
                case KeyEvent.VK_RIGHT:
                    if(pf.getY()%UseInConstants.NoofHBox!=UseInConstants.NoofHBox-1){
                        if(GameFillArr[pf.getY()+1]==-1&&GameFillArr[pf.getX()+1]==-1){
                            if(GameFillArr[pf.getY()]==-1&&GameFillArr[pf.getX()]==-1) {
                                pf.setX(pf.getX()+1);
                                pf.setY(pf.getY()+1);
                            }}}
                    
                    break;
                    
                    
            }
        }
    }
    }
    
    
    
    
    
    
    
    
    
}
//------------------------------------------------------------------

class PairIndex{
    int x;
    int y;
}

/**
 * PairFace object generate Sphere pair and store its state information.
 * and controls it left,right,down movement and rotation operation.
 *
 */

class PairFace{
    int x,y,randomValue;
    int faceX;
    int faceY;
    int pairType;
    public static final int V=1;
    public static final int H=0;
    public static final int IV=3;
    public static final int IH=2;
    PairFace(int x,int y,int pt,int facex,int facey){
        this.x=x;
        this.y=y;
        pairType=pt;
        faceX=facex;
        faceY=facey;
    }
    
    static int startingColumn;
    static PairFace  createNewPairFace(){
        startingColumn=randomColumn();
        startingColumn=(UseInConstants.StartPos/10)*10+startingColumn;
        if((int)(Math.random()*10)<5)
            //return new PairFace(UseInConstants.StartPos,UseInConstants.StartPos+1,H );
            return new PairFace(startingColumn,startingColumn+1,H,random(),random());
        else
            return new PairFace(startingColumn,startingColumn-UseInConstants.WIDTH/25, V,random(),random());
    }
    public int getX(){return x;}
    public void setX(int xnew){
        
        if(xnew>=0)
            this.x=xnew;
    }
    public int getY(){
        
        return y;
    }
    public void setY(int ynew){
        
        if(ynew>=0)
            this.y=ynew;}
    public int getFaceX(){
        return faceX;
    }
    public int getFaceY(){
        return faceY;
    }
    public int getPairType(){
        return pairType;
        
    }
    public void setPairType(int pt){
        pairType=pt;
    }
    public void setFaceX(int faceX){
        this.faceX=faceX;
    }
    public void setFaceY(int faceY){
        this.faceY=faceY;
    }
    private static int randomColumn(){
        int randomValue=(int)(Math.random()*10000);
        return randomValue%10;
    }
    private static int random(){
        return ((int)(Math.random()*10))%4;
    }
    public int getOrientation(){
        return pairType;
    }
    public void setOrientation(int pairType){}
    private void HToV() {
        setX(getY()-UseInConstants.NoofHBox);
    }
    
    public void rotate(Graphics g){
        int x,y;
        if(pairType==H) {
            if(getY()>UseInConstants.NoofHBox&&PuyoGame.getGameFillArr()[getY()+UseInConstants.NoofHBox]==-1){
                x=getX();
                if(x%UseInConstants.NoofHBox==0) {
                    pairType=V;
                    setX(getY()+UseInConstants.NoofHBox);
                } else {
                    pairType=V;
                    setX(getY()+UseInConstants.NoofHBox);
                }
            }
        } else if(pairType==V) {
            if(getY()>UseInConstants.NoofHBox&&PuyoGame.getGameFillArr()[getY()+1]==-1){
                x=getX();
                if(x%UseInConstants.NoofHBox==(UseInConstants.NoofHBox-1)){
                    pairType=IV;
                    setX(getX()-UseInConstants.NoofHBox);
                    setY(getY()-UseInConstants.NoofHBox);
                } else {
                    pairType=IH;
                    setY(getY()+1);
                    setX(getX()-UseInConstants.NoofHBox);
                }
                //swapping
                x=faceX;
                faceX=faceY;
                faceY=x;
                //
            }
        } else if(pairType==IH) {
            if(getX()>UseInConstants.NoofHBox&&PuyoGame.getGameFillArr()[getX()-UseInConstants.NoofHBox]==-1){
                x=getX();
                if((x%UseInConstants.NoofHBox)==0) {
                    pairType=IV;
                    setY(getX()-UseInConstants.NoofHBox);
                } else {
                    pairType=IV;
                    setY(getX()-UseInConstants.NoofHBox);
                }
            }
        } else if(pairType==IV){
            if(getX()>UseInConstants.NoofHBox&&PuyoGame.getGameFillArr()[getX()-1]==-1){
                y=getY();
                if((y%UseInConstants.NoofHBox)==0){
                    pairType=V;
                    //PuyoGame.getGameFillArr()[getX()]=-1;
                    setX(getX()+UseInConstants.NoofHBox);
                    setY(getY()+UseInConstants.NoofHBox);
                    
                } else {
                    pairType=H;
                    setX(getX()-1);
                    setY(getY()+UseInConstants.NoofHBox);
                    
                }
                //swapping
                x=faceX;
                faceX=faceY;
                faceY=x;
                //
            }
        }
    }
    
    
    
    /*
    private PairFace GeneratePairFaceUsingAI(){
        int[] chainLen1=new int[4];
        int[] chainLen2=new int[4];
        int chainLen1Counter=0,chainLen2Counter=0;
        PuyoGame.GameController gameController=PuyoGame.getGameController();
        int gameFillArr[]=PuyoGame.getGameFillArr();
        for(int i=0;i<gameFillArr.length;i++){
            if(gameFillArr[i]==-1){
                gameController.stackTop=-1;
                gameController.vectorCount=0;
                gameController.initVector_Stack();
                gameController.evaluator(i);
                if(gameController.vectorCount==UseInConstants.BondSize-1){
                   if(chainLen1Counter<3)
                   chainLen1[chainLen1Counter++] =gameController.sameFaceVector[0];
                }
                if(gameController.vectorCount==UseInConstants.BondSize-2){
                   if(chainLen2Counter<3)
                   chainLen1[chainLen2Counter++] =gameController.sameFaceVector[0];
                }
            }
     
        }
        return null;
     }
     */
    private static int returnColumnTop(int i){
        for(int j=0;j<UseInConstants.StartPos;j+=UseInConstants.NoofHBox) {
            if(PuyoGame.getGameFillArr()[j+1]==-1)
                return j;
        }
        return i;
    }
    
    public static PairFace createPairFaceUsingAI(){
        int[] gameFillArr=PuyoGame.getGameFillArr();
        int[] boundryElementArr=new int[400];
        int boundryEleCounter=0;
        for(int i=0;i<boundryElementArr.length;i++)
            boundryElementArr[i]=-1;
        boolean var=false;
        for(int i=0;i<(gameFillArr.length-UseInConstants.NoofHBox);i++) {
            var=false;
            if(gameFillArr[i]!=-1) {
                if(((i%UseInConstants.NoofHBox)!=(UseInConstants.NoofHBox-1))&&(i+1)<gameFillArr.length&&gameFillArr[i+1]==-1)
                    var=true;
                if((i%UseInConstants.NoofHBox)!=0&&(i-1)>=0&&gameFillArr[i-1]==-1)
                    var=true;
                if((i-UseInConstants.NoofHBox)>=0&&gameFillArr[i-UseInConstants.NoofHBox]==-1)
                    var=true;
                if((i+UseInConstants.NoofHBox)<gameFillArr.length&&gameFillArr[i+UseInConstants.NoofHBox]==-1)
                    var=true;
                if(var){
                    boundryElementArr[boundryEleCounter++]=i;
                }
            }
        }
       /*
       for(int i=0;i<boundryElementArr.length;i++)
       {
          if(boundryElementArr[i]==-1)
               break;
          System.out.println(":"+boundryElementArr[i]);
       } */
        int[] finalBoundryArr=new int[100];
        
        finalBoundryArr=returnEmptyArray(finalBoundryArr);
        int finalBoundryArrCounter=0;
        for(int i=0;i<boundryElementArr.length;i++) {
            if(boundryElementArr[i]!=-1){
                if((boundryElementArr[i]-1)>=0 &&(gameFillArr[boundryElementArr[i]-1]==-1) &&(boundryElementArr[i])%UseInConstants.NoofHBox!=0){
                    if(((boundryElementArr[i]-1)- UseInConstants.NoofHBox*2)>=0){
                        if(gameFillArr[(boundryElementArr[i]-1)-UseInConstants.NoofHBox*2]!=-1)
                            finalBoundryArr[finalBoundryArrCounter++]=boundryElementArr[i];} else
                                finalBoundryArr[finalBoundryArrCounter++]=boundryElementArr[i];
                } else if((boundryElementArr[i]+1)<UseInConstants.TotalBox && gameFillArr[boundryElementArr[i]+1]==-1&&(boundryElementArr[i]+1)%UseInConstants.NoofHBox!=0){
                    if(((boundryElementArr[i]+1)-UseInConstants.NoofHBox*2)>=0){
                        if(gameFillArr[(boundryElementArr[i]+1)-UseInConstants.NoofHBox*2]!=-1)
                            finalBoundryArr[finalBoundryArrCounter++]=boundryElementArr[i];
                    } else
                        finalBoundryArr[finalBoundryArrCounter++]=boundryElementArr[i];
                } else{
                    finalBoundryArr[finalBoundryArrCounter++]=boundryElementArr[i];
                }
            }
        }
        //System.out.println("Final Array:");
/*       for(int i=0;i<finalBoundryArr.length;i++){
           System.out.println(finalBoundryArr[i]);}*/
        
        int[] rejectedPuyo1=new int[20];
        int rejectedPuyo1Counter=0;
        int[] rejectedPuyo2=new int[20];
        int rejectedPuyo2Counter=0;
        rejectedPuyo1=returnEmptyArray(rejectedPuyo1);
        rejectedPuyo2=returnEmptyArray(rejectedPuyo2);
        evaluatedArray=returnEmptyArray(evaluatedArray);
        for(int i=0;i<finalBoundryArr.length;i++) {
            vectorCount=0;
            stackTop=-1;
            evaluatedArrayCounter=0;
            initVector_Stack();
            if(finalBoundryArr[i]==-1)
                break;
            if(searchArray(evaluatedArray,finalBoundryArr[i])!=-1)
                continue;
            evaluator(finalBoundryArr[i]);
            if(vectorCount==UseInConstants.BondSize-1){
                rejectedPuyo1[rejectedPuyo1Counter++]=gameFillArr[sameFaceVector[0]];
            }
            if(vectorCount==UseInConstants.BondSize-2){
                rejectedPuyo2[rejectedPuyo2Counter++]=gameFillArr[sameFaceVector[0]];
            }
        }
/*       for(int i=0;i<10;i++){
           System.out.println(rejectedPuyo1[i]+":"+rejectedPuyo2[i]);
       }*/
        
        return selectPair(rejectedPuyo1,rejectedPuyo2);
        // return new PairFace(150,150-UseInConstants.WIDTH/25, V,random(),random());
    }
    static int[] selected=new int[2];
    
    private static PairFace selectPair(int[] arr1,int[] arr2){
        
        int selectedCounter=0;
        int[] selected1=new int[4];
        int selected1Counter=0;
        int[] selected2=new int[4];
        int selected2Counter=0;
        selected1=returnEmptyArray(selected1);
        selected2=returnEmptyArray(selected2);
        
        for(int i=0;i<4;i++){
            if(searchArray(arr1,i)!=-1)
                selected1[selected1Counter++]=i;
            if(searchArray(arr2,i)!=-1)
                selected2[selected2Counter++]=i;
        }
    /*    for(int i=0;i<4;i++)
            System.out.println("->"+selected1[i]+":"+selected2[i]);*/
        int xface=selected[0];
        int yface=selected[1];
        selected=returnEmptyArray(selected);
        for(int i=0;i<4;i++) {
            if(selectedCounter>1)
                break;
            if(searchArray(selected2,i)==-1&&searchArray(selected1,i)==-1){
                selected[selectedCounter++]=i;
              //  if(selectedCounter<2)
              //  selected[selectedCounter++]=i;    
            }
        }
        for(int i=0;i<4;i++) {
            if(selectedCounter>1)
                break;
            if(searchArray(selected2,i)!=-1&&searchArray(selected1,i)==-1)
                selected[selectedCounter++]=i;
        }
        
        if(selectedCounter<2)
            for(int i=selectedCounter;i<2&&selectedCounter<2;i++)
                selected[selectedCounter++]=random();
        
        //System.out.println(selected[0]+":"+selected[1]);
        if(selected[0]==xface&&selected[1]==yface)
            selected[1]=random();
        startingColumn=randomColumn();
        startingColumn=(UseInConstants.StartPos/10)*10+startingColumn;
        if((int)(Math.random()*10)<5)
            //return new PairFace(UseInConstants.StartPos,UseInConstants.StartPos+1,H );
            return new PairFace(startingColumn,startingColumn+1,H,selected[0],selected[1]);
        else
            return new PairFace(startingColumn,startingColumn-UseInConstants.WIDTH/25, V,selected[0],selected[1]);
    }
    private static int countElement(int[] arr,int e){
        int count=0;
        for(int i=0;i<arr.length;i++){
            if(arr[i]==e)
                count++;
        }
        return count;
    }
    private static int[] returnEmptyArray(int[] arr){
        for(int i=0;i<arr.length;i++)
            arr[i]=-1;
        return arr;
    }
    private static int searchArray(int[] arr,int key){
        for(int i=0;i<arr.length;i++){
            if(arr[i]==-1)
                break;
            if(arr[i]==key)
                return i;
        }
        return -1;
    }
    
    
    static int[] sameFaceVector=new int[UseInConstants.TotalBox];
    static int[] stack=new int[UseInConstants.TotalBox];
    static int vectorCount=0;
    static int stackTop=-1;
    static int[] evaluatedArray=new int[UseInConstants.TotalBox*2];
    static int evaluatedArrayCounter=0;
    public static void evaluator(int i){
        int[] GameFillArr=PuyoGame.getGameFillArr();
        if(i>=UseInConstants.TotalBox)
            return;
        sameFaceVector[vectorCount++]=i;
        while(true){
            if(searchArray(sameFaceVector,i+1)==-1&&GameFillArr[i+1]==GameFillArr[i]){
                sameFaceVector[vectorCount++]=i+1;
                stack[++stackTop]=i+1;
                evaluatedArray[evaluatedArrayCounter++]=i+1;
            }
            if((i-1)>=0&&searchArray(sameFaceVector,i-1)==-1&&GameFillArr[i-1]==GameFillArr[i]){
                sameFaceVector[vectorCount++]=i-1;
                stack[++stackTop]=i-1;
                evaluatedArray[evaluatedArrayCounter++]=i-1;
            }
            if(searchArray(sameFaceVector,i+UseInConstants.NoofHBox)==-1&&GameFillArr[i+UseInConstants.NoofHBox]==GameFillArr[i]) { //  System.out.println("i="+i+"\n3");
                sameFaceVector[vectorCount++]=(i+UseInConstants.NoofHBox);
                stack[++stackTop]=(i+UseInConstants.NoofHBox);
                evaluatedArray[evaluatedArrayCounter++]=i+UseInConstants.NoofHBox;
            }
            if((i-UseInConstants.NoofHBox)>=0 &&searchArray(sameFaceVector,i-UseInConstants.NoofHBox)==-1 && GameFillArr[i-UseInConstants.NoofHBox]==GameFillArr[i]){
                sameFaceVector[vectorCount++]=(i-UseInConstants.NoofHBox);
                stack[++stackTop]=(i-UseInConstants.NoofHBox);
                evaluatedArray[evaluatedArrayCounter++]=i-UseInConstants.NoofHBox;
            }
            if(stackTop>=0 && stack[stackTop]!=-1) {
                i=stack_pop();
                stackTop--;
            } else break;
        }
    }
    static int stack_pop(){
        int val=stack[0];
        for(int i=0;i<stack.length-1;i++){
            stack[i]=stack[i+1];
        }
        stack[stack.length-1]=-1;
        return val;
    }
    static void initVector_Stack() {
        for(int i=0;i<stack.length;i++)
            stack[i]=-1;
        for(int i=0;i<sameFaceVector.length;i++)
            sameFaceVector[i]=-1;
        
    }
    
    private static void processGameState(int i){}
       
    private static boolean isExist(int[] arr,int key){
        for(int i=0;i<arr.length;i++){
            if(arr[i]==key)
                return true;
        }
        return false;
    }
}
/**
 * Panel controls to display final score of completed session.
 */
class ScorePanel extends javax.swing.JPanel implements ActionListener{
    
    private JPanel parentWindow;
    private JPanel scoreBoard ;
    private File f;
    /**
     * ScorePanel object Constructor
     */
    public ScorePanel(JPanel parent ){
        parentWindow=parent;
        this.scoreBoard=scoreBoard;
        initComponents();
    }
    /**
     * refresh score panel to new values
     */
    public void setScore(int score,int level,int highest){
        jLabel4.setText(score+"");
        jLabel5.setText(level+"");
        jLabel6.setText(highest+"");
    }
    /**
     * initialize use in controls.
     */
    private void initComponents() {
        
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton(new ImageIcon(image));
        jButton2 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        setLayout(null);
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(41, 126, 219),3));
        jLabel1.setFont(new java.awt.Font("Arial Black", Font.PLAIN, 11));
        jLabel1.setForeground(new java.awt.Color(0, 0, 153));
        jLabel1.setText("Your Score :");
        add(jLabel1);
        jLabel1.setBounds(20, 30, 80, 17);
        jLabel2.setFont(new java.awt.Font("Arial Black", Font.PLAIN, 11));
        jLabel2.setForeground(new java.awt.Color(0, 0, 153));
        jLabel2.setText("Level :");
        add(jLabel2);
        jLabel2.setBounds(20, 50, 50, 17);
        jLabel3.setFont(new java.awt.Font("Arial Black", Font.PLAIN, 11));
        jLabel3.setForeground(new java.awt.Color(0, 0, 153));
        jLabel3.setText("Highest Score :");
        //add(jLabel3);
        jLabel3.setBounds(20, 70, 92, 17);
        jButton1.addActionListener(this);
        add(jButton1);
        jButton1.setBounds(150, 9, 10, 10);
        jButton2.setText("Exit");
        jButton2.setBounds(110, 100, 70, 23);
        
        jLabel5.setFont(new java.awt.Font("Arial Black", Font.PLAIN, 11));
        jLabel5.setText("0");
        add(jLabel5);
        jLabel5.setBounds(120, 50, 34, 14);
        
        jLabel4.setFont(new java.awt.Font("Arial Black", Font.PLAIN, 11));
        jLabel4.setText("0");
        add(jLabel4);
        jLabel4.setBounds(120, 30, 34, 14);
        
        jLabel6.setFont(new java.awt.Font("Arial Black", Font.PLAIN, 11));
        jLabel6.setText("0");
        // add(jLabel6);
        jLabel6.setBounds(120, 70, 34, 14);
        
        jLabel7.setFont(new java.awt.Font("Courier New",Font.PLAIN, 14));
        jLabel7.setText("SCORE BOARD");
        add(jLabel7);
        jLabel7.setBounds(10, 4, 100, 20);
        
        
    }
    
    public void actionPerformed(ActionEvent ae){
        parentWindow.remove(this);
        
    }
    
    
    static BufferedImage image;
    public javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    // End of variables declaration
    
}


/**
 * Panel Control object representing GameOver Message.
 */


class GameOverPanel extends javax.swing.JPanel implements ActionListener{
    
    
    private JPanel parentWindow;
    private JPanel scoreBoard;
    /**
     * GameOverPanel object constructor.
     */
    public GameOverPanel(JPanel parent,JPanel scoreBoard){
        parentWindow=parent;
        this.scoreBoard=scoreBoard;
        initComponents();
    }
    
    boolean imageSetter=true;
    /**
     * action event consumer method to display score panel.
     */
    public void actionPerformed(ActionEvent ae){
        parentWindow.add(scoreBoard);
        scoreBoard.setBounds(80,200,165,100);
        try{
            if(imageSetter){
                jButton1.setIcon(new ImageIcon(ImageIO.read(new File("image/up.jpg"))));
                imageSetter=false;
            }else{
                jButton1.setIcon(new ImageIcon(ImageIO.read(new File("image/arr1.jpg"))));
                ((ScorePanel)scoreBoard).actionPerformed(null);
                imageSetter=true;
            }
            
        }catch(Exception e){e.printStackTrace();}
        
    }
    /**
     * initialize use in controls.
     */
    private void initComponents() {
        
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        
        setLayout(null);
        jButton1.addActionListener(this);
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 255), 3));
        jLabel1.setFont(new java.awt.Font("Comic Sans MS", Font.PLAIN, 18));
        jLabel1.setForeground(new java.awt.Color(41, 126, 219));
        jLabel1.setText("Game Over");
        add(jLabel1);
        jLabel1.setBounds(20, 10, 100, 20);
        
        jButton1.setIcon(new javax.swing.ImageIcon("image/arr1.JPG"));
        jButton1.setBounds(120, 10, 20, 20);
        add(jButton1);
        setOpaque(false);
    }
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration
    
}
/**
 * Panel Control to display current score,next sphere pair and game mode
 *
 */


class CurrentScorePanel extends javax.swing.JPanel{
    
    private JPanel parentPanel;
    private Graphics g;
    private PairFace pf;
    /**
     * CurrentScorePanel object Constructor.
     */
    public CurrentScorePanel(JPanel jPanel,Graphics gArg) {
        g=gArg;
        parentPanel=jPanel;
        initComponents();
        
    }
    /**
     * called by java platform gui manager during paining operation
     */
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        this.g=g;
        g.setColor(Color.white);
        if(g!=null){
            if(UseInConstants.GameMode==UseInConstants.AIMode){
                
                g.fillRect(190,5,UseInConstants.FaceSize,UseInConstants.FaceSize);
                g.fillRect(190,5+UseInConstants.FaceSize,UseInConstants.FaceSize,UseInConstants.FaceSize);
                g.fillRect(190,5,UseInConstants.FaceSize,UseInConstants.FaceSize);
                g.fillRect(190+UseInConstants.FaceSize,5,UseInConstants.FaceSize,UseInConstants.FaceSize);
            } else if(pf.getPairType()==PairFace.H){
                
                
                g.fillRect(190,5,UseInConstants.FaceSize,UseInConstants.FaceSize);
                g.fillRect(190,5+UseInConstants.FaceSize,UseInConstants.FaceSize,UseInConstants.FaceSize);
                g.drawImage(PuyoGame.getImageArray()[pf.getFaceX()],190,5,UseInConstants.FaceSize,UseInConstants.FaceSize,null);
                g.drawImage(PuyoGame.getImageArray()[pf.getFaceY()],190+UseInConstants.FaceSize,5,UseInConstants.FaceSize,UseInConstants.FaceSize,null);} else{
                
                
                g.fillRect(190,5,UseInConstants.FaceSize,UseInConstants.FaceSize);
                g.fillRect(190+UseInConstants.FaceSize,5,UseInConstants.FaceSize,UseInConstants.FaceSize);
                g.drawImage(PuyoGame.getImageArray()[pf.getFaceX()],190,5,UseInConstants.FaceSize,UseInConstants.FaceSize,null);
                g.drawImage(PuyoGame.getImageArray()[pf.getFaceY()],190,5+UseInConstants.FaceSize,UseInConstants.FaceSize,UseInConstants.FaceSize,null);
                }
        }
    }
    
    /**
     * set PairFace object
     */
    
    public void setNextPair(PairFace pf){
        this.pf=pf;
    }
    /**
     * Refresh current score.
     */
    public void resetScore(int score,int level){
        jLabel4.setText(score+"");
        jLabel5.setText(level+"");
    }
    /**
     * initialize use in controls.
     */
    private void initComponents() {
        
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel("");
        jLabel7 = new javax.swing.JLabel("");
        jLabel8 = new javax.swing.JLabel("0");
        jLabel9 = new javax.swing.JLabel("/0 kb");
        jLabel10 = new javax.swing.JLabel("Free Memory:");
        jLabel11 = new javax.swing.JLabel("Chain Length:");
        jLabel12 = new javax.swing.JLabel("Mode- ");
        jButton1 = new javax.swing.JButton();
        
        setLayout(null);
        setFocusable(false);
        // setBackground(Color.cyan);
        //addKeyListener(PuyoGame.klistener);
        
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(100, 102, 102), 5, true));
        jLabel1.setFont(new java.awt.Font("Lucida Console", Font.BOLD, 11));
        jLabel1.setText("SCORE-");
        jLabel1.setForeground(Color.blue);

        add(jLabel1);
        jLabel1.setBounds(10, 10, 50, 12);
        
        jLabel2.setFont(new java.awt.Font("Lucida Console", Font.BOLD, 11));
        jLabel2.setForeground(Color.blue);
        jLabel2.setText("LEVEL-");
        add(jLabel2);
        jLabel2.setBounds(90, 10, 50, 12);
        
        jLabel3.setFont(new java.awt.Font("Lucida Console", Font.PLAIN, 11));
        jLabel3.setForeground(Color.blue);

        jLabel3.setText("Next");
        add(jLabel3);
        jLabel3.setBounds(160, 10, 50, 12);
        jLabel6.setBounds(210, 0,UseInConstants.FaceSize-10 ,UseInConstants.FaceSize-10);
        jLabel7.setBounds(210+UseInConstants.FaceSize, 0,UseInConstants.FaceSize ,UseInConstants.FaceSize);
        jLabel4.setFont(new java.awt.Font("Lucida Console", Font.BOLD, 11));
        jLabel4.setText("0");
        jLabel8.setFont(new java.awt.Font("Lucida Console", Font.PLAIN, 11));
        jLabel8.setBounds(100, 24, 110, 12);
        
        jLabel8.setText((int)(Runtime.getRuntime().freeMemory()/1000)+"");
        jLabel9.setFont(new java.awt.Font("Lucida Console", Font.PLAIN, 11));
        jLabel9.setBounds(130, 24, 160, 12);
        jLabel9.setText("/"+(int)(Runtime.getRuntime().totalMemory()/1000)+"kb");
        jLabel10.setBounds(10, 22, 95, 12);
        jLabel10.setFont(new java.awt.Font("Lucida Console", Font.PLAIN, 11));
        jLabel11.setFont(new java.awt.Font("Lucida Console", Font.PLAIN, 11));
        jLabel12.setFont(new java.awt.Font("Lucida Console", Font.PLAIN, 11));
        jLabel11.setBounds(10, 42, 120, 12);
        jLabel12.setBounds(230, 25,100,12);
        add(jLabel11);
        add(jLabel12);
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox1.setMaximumRowCount(8);
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });
        
        jComboBox1.setForeground(new java.awt.Color(0, 51, 51));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        jComboBox1.setToolTipText("");
        jComboBox1.setFocusable(false);
        jComboBox1.setSelectedIndex(3);
        jComboBox1.setBounds(100,40,35,16);
        add(jComboBox1);
        jComboBox2 = new javax.swing.JComboBox();
        
        
        
        jComboBox2.setForeground(new java.awt.Color(0, 51, 51));
        jComboBox2.setMaximumRowCount(4);
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Random", "A.I." }));
        jComboBox2.setToolTipText("");
        jComboBox2.setFocusable(false);
        jComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox2ItemStateChanged(evt);
            }
        });
        jComboBox2.setBounds(225, 40, 75, 16);
        add(jComboBox2);
        add(jLabel4);
        add(jLabel6);
        add(jLabel7);
        add(jLabel8);
        add(jLabel9);
        add(jLabel10);
        jLabel4.setBounds(60, 10, 30, 12);
        jLabel5.setFont(new java.awt.Font("Lucida Console",Font.BOLD , 11));
        jLabel5.setText("0");
        add(jLabel5);
        jLabel5.setBounds(140, 10, 30, 12);
        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon("image/switch.jpg"));
        //jButton1.setToolTipText("Click To Hide");
        
        jButton1.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jButton1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jButton1.setBounds(283, 5, 20, 18);
        add(jButton1);
        add(addMinimizeButton());
    }
    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt){
        //System.out.println("item state changed1");
        jComboBox1=(JComboBox)evt.getSource();
        try{
            UseInConstants.BondSize=Integer.parseInt((String)jComboBox1.getSelectedItem());
        }catch(Exception e){e.printStackTrace();}
    }
    private void jComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {
        //System.out.println("item state changed2");
        jComboBox2=(JComboBox)evt.getSource();
        if(jComboBox2.getSelectedIndex()==0){
            //System.out.println("equal");
            add(jLabel3);
            UseInConstants.GameMode=UseInConstants.SimpleMode;} else{  //System.out.println("not equal");
            remove(jLabel3);
            UseInConstants.GameMode=UseInConstants.AIMode;
            }
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        
    }
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        
    }
    public void setFreeMemory(long free){
        jLabel8.setText((int)(free/1000)+"");
    }
    public void setTotalMemory(long total){
        jLabel9.setText("/"+(int)(total/1000)+"kb");
    }
    /**
     * Add minimize button
     */
    
    private JButton addMinimizeButton(){
        jButton2=new JButton();
        jButton2.setBackground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon("image/compensate.jpg"));
        jButton2.setToolTipText("Click To Hide");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jButton2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 1, true));
        jButton2.setBounds(263, 5, 20, 18);
        return jButton2;
        
        
    }
    private javax.swing.JButton jButton1,jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
}

/**
 * Panel control to display to ask about Quit opration.
 */

class QuitPanel extends javax.swing.JPanel {
    
    
    private JPanel parentPanel;
    /**
     * QuitPanel object constructor.
     */
    public QuitPanel(JPanel jPanel){
        parentPanel=jPanel;
        initComponents();
    }
    
    /**
     * initialize use in controls.
     */
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        setLayout(null);
        
        
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 2, true));
        jLabel1.setFont(new java.awt.Font("Lucida Console", Font.PLAIN, 14));
        jLabel1.setText(" Do you want to Quit?");
        
        add(jLabel1);
        
        jLabel1.setBounds(0, 20, 195, 14);
        jLabel1.setForeground(new java.awt.Color(0, 102, 102));
        jButton1.setText("YES");
        jButton1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        jButton1.setForeground(new java.awt.Color(0, 102, 102));
        add(jButton1);
        jButton1.setBounds(20, 50, 60, 17);
        
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jButton2.setText("NO");
        jButton2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        jButton2.setForeground(new java.awt.Color(0, 102, 102));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        
        add(jButton2);
        jButton2.setBounds(90, 50, 50, 17);
        
    }
    
    /**
     * called when No button is pressed.
     */
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        parentPanel.remove(this);
        
    }
    /**
     * Called when Yes button is pressed.
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        
        parentPanel.remove(this);
        System.exit(0);
    }
    
    
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    
    
}
/**
 * Panel represening ProgressBarPanel
 */
class ProgressBarPanel extends javax.swing.JPanel{
    
    /**
     * ProgrssBarPanel object constructor.
     */
    public ProgressBarPanel(){
        initComponents();
    }
    
    /**
     * initialize usein controls.
     */
    private void initComponents() {
        
        jButton1 = new javax.swing.JButton("Start");
        jButton2 = new javax.swing.JButton("New");
        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar1.setBackground(new java.awt.Color(255, 255, 255));
        setLayout(null);
        setBackground(new java.awt.Color(255, 255, 255));
        jProgressBar1.setForeground(new java.awt.Color(0, 153, 153));
        jProgressBar1.setFocusable(false);
        jProgressBar1.setMaximum(50);
        jProgressBar1.setMinimum(0);
        add(jProgressBar1);
        jButton1.setBounds(150, 10, 72, 18);
        jButton1.setFocusable(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1);
        jButton2.setBounds(222, 10, 62, 18);
        jButton2.setFocusable(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        add(jButton2);
        jProgressBar1.setBounds(20,10,120,18);
    }
    /**
     * return progressbar panel
     */
    public JProgressBar getProgressBarObject(){
        return jProgressBar1;
    }
    
    boolean start=true;
    /*
     *called when pause or start button is pressed
     */
    private synchronized void jButton1ActionPerformed(java.awt.event.ActionEvent evt){
        synchronized(this){
            notifyAll();}
        
        if(!start){
            jButton1.setText("Start");start=true;
            //jButton1.setToolTipText("Start");
            PuyoGame.paused=true;
        } else{
            jButton1.setText("Pause");start=false;
            //jButton1.setToolTipText("Pause");
            
            PuyoGame.paused=false;
            synchronized(this){
                notifyAll();}
            
        }
        
    }
    
    
    /**
     * called when new button is pressed.
     */
    
    private synchronized void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        jButton1.setEnabled(true);
        PuyoGame.setGameFillArr();
        PuyoGame.setGameScoreValue(0);
        PuyoGame.setGamelevelValue(0);
        PuyoGame.getCurrentScorePanel().resetScore(0,0);
        UseInConstants.TIMERDELAY=500;
        setProgressBar(0);
        PuyoGame.getCurrentPairFace().setX(UseInConstants.TotalBox-34);
        PuyoGame.getCurrentPairFace().setY(UseInConstants.TotalBox-33);
        if(PuyoGame.getCurrentPairFace().getFaceX()<3)
            PuyoGame.getCurrentPairFace().setFaceX(PuyoGame.getCurrentPairFace().getFaceX()+1);
        if(PuyoGame.getCurrentPairFace().getFaceY()<3)
            PuyoGame.getCurrentPairFace().setFaceY(PuyoGame.getCurrentPairFace().getFaceY()+1);
        start=false;
        PuyoGame.getGameController().remove(PuyoGame.getGameController().getGameOverPanel());
        PuyoGame.getGameController().remove(PuyoGame.getGameController().getScorePanel());
        jButton1ActionPerformed(null);
    }
    /**
     * set ProgressBar value.
     */
    public void setProgressBar(int val){
        jProgressBar1.setValue(val);
    }
    public JButton getNewButton(){
        return jButton2;
    }
    public JButton getStartButton(){
        return jButton1;
    }
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    
}
/**
 * Listener for window Events.
 */
class GameWindowEventListener implements WindowListener{
    
    public void windowClosing(WindowEvent we){
    }
    
    public void windowDeactivated(WindowEvent we){
        //System.out.println("window deactivated");
        PuyoGame.windowIconofied=true;
    }
    
    public synchronized void windowActivated(WindowEvent we){
        //    System.out.println("window activated");
        PuyoGame.windowIconofied=false;
        notifyAll();
    }
    
    public synchronized void windowDeiconified(WindowEvent we){
        //   System.out.println("window deiconified");
        PuyoGame.windowIconofied=false;
        notifyAll();
    }
    
    public void windowIconified(WindowEvent we){
        //    System.out.println("window iconified");
        PuyoGame.windowIconofied=true;
        
    }
    
    public void windowClosed(WindowEvent we){
        //  System.out.println("window closed");
    }
    
    public void windowOpened(WindowEvent we){
        //   System.out.println("window opened");
    }
}