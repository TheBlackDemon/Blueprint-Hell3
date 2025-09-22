package Game;

import client.ClientMain;
import controller.AudioManager;
import network.GameStateData;
import view.GameOverPanel;
import view.ShopPanel;
import view.SuccessfullyPanel;
import view.Window;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private ClientMain client;
    private boolean onlineMod;
    private final GameState state;
    private final IWireLengthManager wireLengthManager;
    private final IPacketManager packetManager;
    private final IConnectionValidator validator;
    private final NodeRenderer nodeRenderer;
    private final ConnectionRenderer connectionRenderer;
    private final PacketRenderer packetRenderer;
    private final ShockwaveRenderer shockwaveRenderer;
    private final WireLengthRenderer wireLengthRenderer;
    private INode draggingNode;
    private Point dragOffset;
    private INode draggingFromNode;
    private int draggingFromPort;
    private Point dragEnd;
    private Point draggingWaypoint;
    private IConnection draggingWaypointConnection;
    private BendPoint draggingBendPoint;
    private IConnection draggingBendPointConnection;
    private Point lastValidNodePosition;
    private Point lastValidWaypointPosition;
    private Point lastValidBendPointPosition;
    private Timer animationTimer = null;
    private boolean isCPressed;
    private boolean isZPressed;
    private boolean isNPressed;
    private boolean isAPressed;
    private boolean start = false;
    private boolean scrollAergiaMode = false;
    private boolean scrollSisyphusMode = false;
    private boolean scrollEliphasMode = false;
    private SaveManager saveManager;

    public GamePanel(ClientMain client , boolean onlineMod , GameState state, IWireLengthManager wireLengthManager, IPacketManager packetManager,
                     IConnectionValidator validator) {
        this.setSize(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.setLayout(null);
        this.setFocusable(true);
        this.requestFocus();
        this.requestFocusInWindow();
        this.client = client;
        this.onlineMod = onlineMod;
        this.state = state;
        this.wireLengthManager = wireLengthManager;
        this.packetManager = packetManager;
        this.validator = validator;
        this.nodeRenderer = new NodeRenderer();
        this.connectionRenderer = new ConnectionRenderer();
        this.packetRenderer = new PacketRenderer();
        this.shockwaveRenderer = new ShockwaveRenderer();
        this.wireLengthRenderer = new WireLengthRenderer(wireLengthManager);
        this.isCPressed = false;
        this.isZPressed = false;
        this.isNPressed = false;
        this.isAPressed = false;
        this.saveManager = new SaveManager();
        initializeUI();
        animationTimer = new Timer(GameConfig.ANIMATION_TICK_MS, e -> {
            if (!state.isGameOver() && !state.isSuccessfully()) {
                    packetManager.stepForward();
                    saveManager.autoSave(state);
                    repaint();
            }else{
                ((Timer)e.getSource()).stop();
                if (state.isGameOver()) {
                    sendGameResult();
                    GameOverPanel gameOverPanel = new GameOverPanel(client , onlineMod , state.getUser(), state.getPacketLoss(), state.getPackets().size());
                    Window.getMainFrame().setContentPane(gameOverPanel);
                    return;
                } else if (state.isSuccessfully()) {
                    sendGameResult();
                    SuccessfullyPanel successfullyPanel = new SuccessfullyPanel(client , onlineMod , state.getUser(), state.getPacketLoss(), state.getPackets().size() , state.getLevel());
                    Window.getMainFrame().setContentPane(successfullyPanel);
                    return;
                }
            }

        });
    }

    private void initializeUI() {
        setBackground(Color.WHITE);
        setLayout(null);

        JButton runButton = new JButton("Run");
        runButton.setBounds(GameConfig.RUN_BUTTON_X, GameConfig.BUTTON_Y, GameConfig.BUTTON_WIDTH, GameConfig.BUTTON_HEIGHT);
        runButton.addActionListener(e -> checkSolution(true));
        add(runButton);

        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "pressR");
        actionMap.put("pressR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkSolution(true);
                checkSolution(true);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false), "pressC");
        actionMap.put("pressC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCPressed = true;
                handleTimeStep();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, true), "releaseC");
        actionMap.put("releaseC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCPressed = false;
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0, false), "pressZ");
        actionMap.put("pressZ", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isZPressed = true;
                handleTimeStep();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0, true), "releaseZ");
        actionMap.put("releaseZ", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isZPressed = false;
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0, false), "pressN");
        actionMap.put("pressN", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isNPressed) {
                    animationTimer.stop();
                }
                isNPressed = true;
                ShopPanel shopPanel = new ShopPanel(client , onlineMod , state.getUser() , GamePanel.this);
                Window.getMainFrame().setContentPane(shopPanel);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveManager.saveGame(state);
                JOptionPane.showMessageDialog(GamePanel.this, "Game saved successfully!", 
                    "Save", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "load");
        actionMap.put("load", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (saveManager.hasSaveFile()) {
                    if (saveManager.loadGame(state)) {
                        JOptionPane.showMessageDialog(GamePanel.this, "Game loaded successfully!", 
                            "Load", JOptionPane.INFORMATION_MESSAGE);
                        repaint();
                    } else {
                        JOptionPane.showMessageDialog(GamePanel.this, "Failed to load game!", 
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(GamePanel.this, "No save file found!", 
                        "Load Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        setupMouseListeners();
    }

    private void handleTimeStep() {
        if (isCPressed && isZPressed) {
            return;
        }
        animationTimer.stop();
        if (isCPressed) {
            packetManager.stepForward();
        } else if (isZPressed) {
            packetManager.stepBackward();
        }
        repaint();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                GameLogger.logUserAction("mouse_press", String.format("Button: %d, Position: (%d, %d)", 
                                   e.getButton(), e.getX(), e.getY()), true);
                
                if (e.getButton() == MouseEvent.BUTTON1) {
                    handleLeftClick(e);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    handleRightClick(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                GameLogger.logUserAction("mouse_release", String.format("Button: %d, Position: (%d, %d)", 
                                   e.getButton(), e.getX(), e.getY()), true);
                
                if (e.getButton() == MouseEvent.BUTTON1 && draggingFromNode != null) {
                    handleConnectionRelease(e);
                }
                resetDraggingState();
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleDragging(e);
                repaint();
            }
        });
    }

    private void handleLeftClick(MouseEvent e) {
        if (scrollAergiaMode) {
            handleScrollAergiaClick(e);
            return;
        } else if (scrollSisyphusMode) {
            handleScrollSisyphusClick(e);
            return;
        } else if (scrollEliphasMode) {
            handleScrollEliphasClick(e);
            return;
        }
        
        for (INode node : state.getNodes()) {
            if (node.isOverNode(e.getX(), e.getY())) {
                startNodeDragging(node, e);
                return;
            }
        }
        for (INode node : state.getNodes()) {
            for (int i = 0; i < node.getInputShapes().length; i++) {
                if (node.isOverPort(e.getX(), e.getY(), "input", i) && node.getConnectedInputs().contains(i)) {
                    removeConnection(node, i);
                    return;
                }
            }
        }
        for (INode node : state.getNodes()) {
            for (int i = 0; i < node.getOutputShapes().length; i++) {
                if (node.isOverPort(e.getX(), e.getY(), "output", i)) {
                    startConnectionDragging(node, i, e);
                    return;
                }
            }
        }
        for (IConnection conn : state.getConnections()) {
            Point wp = conn.getNearestWaypoint(e.getX(), e.getY());
            if (wp != null) {
                startWaypointDragging(wp, conn);
                return;
            }
        }
        for (IConnection conn : state.getConnections()) {
            BendPoint bp = conn.getNearestBendPoint(e.getX(), e.getY());
            if (bp != null) {
                if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
                    removeBendPoint(bp, conn);
                    return;
                } else {
                    startBendPointDragging(bp, conn);
                    return;
                }
            }
        }
    }

    private void handleRightClick(MouseEvent e) {
        for (IConnection conn : state.getConnections()) {
            if (conn.isNearLine(e.getX(), e.getY())) {
                addBendPoint(conn, e.getX(), e.getY());
                return;
            }
        }
    }

    private void startNodeDragging(INode node, MouseEvent e) {
        draggingNode = node;
        dragOffset = new Point(e.getX() - node.getX(), e.getY() - node.getY());
        lastValidNodePosition = new Point(node.getX(), node.getY());
    }

    private void startConnectionDragging(INode node, int port, MouseEvent e) {
        draggingFromNode = node;
        draggingFromPort = port;
        dragEnd = new Point(e.getX(), e.getY());
    }

    private void startWaypointDragging(Point wp, IConnection conn) {
        draggingWaypoint = wp;
        draggingWaypointConnection = conn;
        lastValidWaypointPosition = new Point(wp.x, wp.y);
    }
    
    private void startBendPointDragging(BendPoint bp, IConnection conn) {
        draggingBendPoint = bp;
        draggingBendPointConnection = conn;
        lastValidBendPointPosition = new Point(bp.getPosition());
        bp.setDragging(true);
    }

    private void removeConnection(INode node, int port) {
        IConnection toRemove = null;
        for (IConnection conn : state.getConnections()) {
            if (conn.getToNode() == node && conn.getToPort() == port) {
                toRemove = conn;
                break;
            }
        }
        if (toRemove != null) {
            state.removeConnection(toRemove);
            node.getConnectedInputs().remove(port);
            animationTimer.stop();
            packetManager.resetPackets();
            System.out.println("Connection removed from " + toRemove.getFromNode().getId() + " to " + node.getId() + ", port " + port);
            JOptionPane.showMessageDialog(this, "Connection removed.", "Connection Updated", JOptionPane.INFORMATION_MESSAGE);
            repaint();
        }
    }

    private void addBendPoint(IConnection conn, int x, int y) {
        if (!conn.canAddBendPoint()) {
            JOptionPane.showMessageDialog(this, "Cannot add bend point: Maximum of 3 bend points per wire.",
                    "Bend Point Limit Exceeded", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (state.getUser().getCoin() < 1) {
            JOptionPane.showMessageDialog(this, "Cannot add bend point: Insufficient coins. Need 1 coin.",
                    "Insufficient Coins", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (conn.intersectsWithAnyNode(state.getNodes())) {
            JOptionPane.showMessageDialog(this, "Cannot add bend point: Would cause intersection with nodes.",
                    "Invalid Position", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (conn.addBendPoint(x, y)) {
            state.getUser().setCoin(state.getUser().getCoin() - 1);
            System.out.println("Bend point added to connection from " + conn.getFromNode().getId() + " to " + conn.getToNode().getId());
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Cannot add bend point: Invalid position.",
                    "Invalid Position", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeBendPoint(BendPoint bendPoint, IConnection conn) {
        if (conn.removeBendPoint(bendPoint)) {
            System.out.println("Bend point removed from connection from " + conn.getFromNode().getId() + " to " + conn.getToNode().getId());
            repaint();
        }
    }

    private void handleConnectionRelease(MouseEvent e) {
        boolean connectionMade = false;
        for (INode node : state.getNodes()) {
            for (int i = 0; i < node.getInputShapes().length; i++) {
                if (node.isOverPort(e.getX(), e.getY(), "input", i)) {
                    IConnection newConn = new Connection(draggingFromNode, draggingFromPort, node, i);
                    if (validator.isValid(newConn)) {
                        if (newConn.intersectsWithAnyNode(state.getNodes())) {
                            JOptionPane.showMessageDialog(this, "Cannot connect: Wire would pass through other nodes.",
                                    "Connection Error", JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                        AudioManager.playSound("connect");
                        connectionMade = tryAddConnection(newConn, node, i);
                        
                        if (connectionMade) {
                            GameLogger.logConnectionCreated(newConn.toString(), 
                                                          draggingFromNode.getId(), 
                                                          node.getId(), 
                                                          newConn.getLength(), 
                                                          draggingFromPort, i);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Cannot connect: Invalid connection).",
                                "Connection Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                }
            }
        }
        if (!connectionMade && !draggingFromNode.isOverPort(e.getX(), e.getY(), "output", draggingFromPort)) {
            JOptionPane.showMessageDialog(this, "Connection failed: No valid input port selected.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean tryAddConnection(IConnection newConn, INode toNode, int toPort) {
        java.util.List<IConnection> tempConnections = new ArrayList<>(state.getConnections());
        if (tempConnections.size() >= state.getNodes().size()*3 && !isReplacingConnection(tempConnections, toNode, toPort)) {
            JOptionPane.showMessageDialog(this, "Cannot connect: Maximum connections allowed.",
                    "Connection Limit Exceeded", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        IConnection toRemove = null;
        for (IConnection conn : tempConnections) {
            if (conn.getToNode() == toNode && conn.getToPort() == toPort) {
                toRemove = conn;
                break;
            }
        }
        if (toRemove != null) {
            tempConnections.remove(toRemove);
        }
        tempConnections.add(newConn);
        double newTotalLength = 0.0;
        for (IConnection conn : tempConnections) {
            newTotalLength += conn.getLength();
        }
        if (wireLengthManager.canAddWireLength(newConn.getLength(), toRemove)) {
            if (toRemove != null) {
                state.removeConnection(toRemove);
                toNode.getConnectedInputs().remove(toPort);
                System.out.println("Existing connection removed from " + toRemove.getFromNode().getId() + " to " + toNode.getId() + ", port " + toPort);
            }
            state.addConnection(newConn);
            toNode.getConnectedInputs().add(toPort);
            animationTimer.stop();
            packetManager.resetPackets();
            System.out.println("Connection added from " + newConn.getFromNode().getId() + " to " + toNode.getId() + ", port " + toPort);
            if (toRemove != null) {
                JOptionPane.showMessageDialog(this, "Existing connection replaced.",
                        "Connection Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Connection added.",
                        "Connection Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Cannot connect: Insufficient wire length.",
                    "Wire Limit Exceeded", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean isReplacingConnection(java.util.List<IConnection> connections, INode toNode, int toPort) {
        for (IConnection conn : connections) {
            if (conn.getToNode() == toNode && conn.getToPort() == toPort) {
                return true;
            }
        }
        return false;
    }

    private void handleDragging(MouseEvent e) {
        if (draggingNode != null && lastValidNodePosition != null) {
            restrictNodeMovement(e);
        } else if (draggingFromNode != null) {
            dragEnd.setLocation(e.getX(), e.getY());
        } else if (draggingWaypoint != null && lastValidWaypointPosition != null) {
            restrictWaypointMovement(e);
        } else if (draggingBendPoint != null && lastValidBendPointPosition != null) {
            restrictBendPointMovement(e);
        }
    }

    private void restrictNodeMovement(MouseEvent e) {
        int newX = e.getX() - dragOffset.x;
        int newY = e.getY() - dragOffset.y;
        int oldX = draggingNode.getX();
        int oldY = draggingNode.getY();
        
        if (scrollSisyphusMode || isScrollSisyphusMovement()) {
            if (isWithinSisyphusRadius(newX, newY)) {
                draggingNode.setPosition(newX, newY);
                lastValidNodePosition.setLocation(newX, newY);
            } else {
                Point originalPos = lastValidNodePosition;
                double angle = Math.atan2(newY - originalPos.y, newX - originalPos.x);
                int constrainedX = (int) (originalPos.x + Math.cos(angle) * GameConfig.SCROLL_SISYPHUS_RADIUS);
                int constrainedY = (int) (originalPos.y + Math.sin(angle) * GameConfig.SCROLL_SISYPHUS_RADIUS);
                draggingNode.setPosition(constrainedX, constrainedY);
            }
        } else {
            draggingNode.setPosition(newX, newY);
            if (wireLengthManager.canAddWireLength(0, null)) {
                lastValidNodePosition.setLocation(newX, newY);
            } else {
                draggingNode.setPosition(oldX, oldY);
            }
        }
    }
    
    private boolean isScrollSisyphusMovement() {
        return draggingNode != null && !draggingNode.getId().equals("A") && !draggingNode.getId().equals("C");
    }
    
    private boolean isWithinSisyphusRadius(int newX, int newY) {
        if (lastValidNodePosition == null) return true;
        double distance = Math.hypot(newX - lastValidNodePosition.x, newY - lastValidNodePosition.y);
        return distance <= GameConfig.SCROLL_SISYPHUS_RADIUS;
    }

    private void restrictWaypointMovement(MouseEvent e) {
        int oldX = draggingWaypoint.x;
        int oldY = draggingWaypoint.y;
        draggingWaypoint.setLocation(e.getX(), e.getY());
        if (wireLengthManager.canAddWireLength(0, null)) {
            lastValidWaypointPosition.setLocation(e.getX(), e.getY());
        } else {
            draggingWaypoint.setLocation(oldX, oldY);
        }
    }
    
    private void restrictBendPointMovement(MouseEvent e) {
        Point newPosition = new Point(e.getX(), e.getY());
        Point originalPosition = lastValidBendPointPosition;
        
        if (draggingBendPoint.isWithinRadius(originalPosition, newPosition)) {
            draggingBendPoint.setPosition(newPosition);
        } else {
            double angle = Math.atan2(newPosition.y - originalPosition.y, newPosition.x - originalPosition.x);
            int constrainedX = (int) (originalPosition.x + Math.cos(angle) * draggingBendPoint.getMaxRadius());
            int constrainedY = (int) (originalPosition.y + Math.sin(angle) * draggingBendPoint.getMaxRadius());
            draggingBendPoint.setPosition(constrainedX, constrainedY);
        }
    }

    private void resetDraggingState() {
        draggingNode = null;
        draggingFromNode = null;
        draggingFromPort = -1;
        draggingWaypoint = null;
        draggingWaypointConnection = null;
        if (draggingBendPoint != null) {
            draggingBendPoint.setDragging(false);
        }
        draggingBendPoint = null;
        draggingBendPointConnection = null;
        lastValidNodePosition = null;
        lastValidWaypointPosition = null;
        lastValidBendPointPosition = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.drawString("COIN : " + state.getUser().getCoin() , 500 , 20);
        for (IConnection conn : state.getConnections()) {
            connectionRenderer.render(g, conn);
        }
        if (draggingFromNode != null) {
            Point start = draggingFromNode.getPortPosition("output", draggingFromPort);
            g.setColor(new Color(0, 0, 0, 100));
            g.drawLine(start.x, start.y, dragEnd.x, dragEnd.y);
        }
        for (Shockwave shockwave : state.getShockwaves()) {
            shockwaveRenderer.render(g, shockwave);
        }
        for (Packet packet : state.getPackets()) {
            packetRenderer.render(g, packet);
        }
        for (INode node : state.getNodes()) {
            nodeRenderer.render(g, node, state.getConnections());
        }
        wireLengthRenderer.render(g, getWidth());
        g.setColor(Color.orange);
        g.drawString("triangle packet : " + state.getNumberPacketTriangle(), 120, 20);
        g.drawString("square packet : " + state.getNumberPacketsSquare(), 300, 20);

        if (state.isAtar()){
            g.setColor(Color.RED);
            g.drawString("Atar is ON" , 120, 60);
        }
        if (state.isAiryaman()){
            g.setColor(Color.RED);
            g.drawString("Airyaman is ON" , 300, 60);
        }
        
        g.setColor(Color.MAGENTA);
        g.drawString("Right-click on wire to add bend point (1 coin)", 10, getHeight() - 40);
        g.drawString("Shift+click on bend point to remove", 10, getHeight() - 20);
        
        if (scrollAergiaMode) {
            g.setColor(Color.RED);
            g.drawString("Scroll of Aergia Mode: Click on a connection to stop packets", 10, getHeight() - 60);
        } else if (scrollSisyphusMode) {
            g.setColor(Color.BLUE);
            g.drawString("Scroll of Sisyphus Mode: Click on a non-reference system to move it", 10, getHeight() - 60);
        } else if (scrollEliphasMode) {
            g.setColor(Color.GREEN);
            g.drawString("Scroll of Eliphas Mode: Click on a connection to restore packet center of mass", 10, getHeight() - 60);
        }
        
        g.setColor(Color.DARK_GRAY);
        g.drawString("Ctrl+S: Save Game | Ctrl+L: Load Game", 10, getHeight() - 80);
    }

    public void checkSolution(boolean showError) {
        StringBuilder errorMessage = new StringBuilder();
        if (validator.isCorrectPath(state, errorMessage)) {
            if (!animationTimer.isRunning()) {
                animationTimer.start();
//                JOptionPane.showMessageDialog(this, errorMessage.toString() + ". Packets are moving.",
//                        "Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                animationTimer.stop();
                packetManager.resetPackets();
            }
        } else {
            animationTimer.stop();
            packetManager.resetPackets();
            if (showError) {
                JOptionPane.showMessageDialog(this, "Error: " + errorMessage.toString(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isNPressed() {
        return isNPressed;
    }

    public void setNPressed(boolean NPressed) {
        isNPressed = NPressed;
    }

    public Timer getAnimationTimer() {
        return animationTimer;
    }

    public GameState getState() {
        return state;
    }
    
    public void setScrollAergiaMode(boolean scrollAergiaMode) {
        this.scrollAergiaMode = scrollAergiaMode;
        this.scrollSisyphusMode = false;
        this.scrollEliphasMode = false;
    }
    
    public void setScrollSisyphusMode(boolean scrollSisyphusMode) {
        this.scrollSisyphusMode = scrollSisyphusMode;
        this.scrollAergiaMode = false;
        this.scrollEliphasMode = false;
    }
    
    public void setScrollEliphasMode(boolean scrollEliphasMode) {
        this.scrollEliphasMode = scrollEliphasMode;
        this.scrollAergiaMode = false;
        this.scrollSisyphusMode = false;
    }
    
    public boolean isScrollAergiaMode() {
        return scrollAergiaMode;
    }
    
    public boolean isScrollSisyphusMode() {
        return scrollSisyphusMode;
    }
    
    public boolean isScrollEliphasMode() {
        return scrollEliphasMode;
    }
    
    private void handleScrollAergiaClick(MouseEvent e) {
        for (IConnection conn : state.getConnections()) {
            if (conn.isNearLine(e.getX(), e.getY())) {
                String connectionId = getConnectionId(conn);
                state.addScrollAergiaEffect(connectionId, System.currentTimeMillis());
                scrollAergiaMode = false;
                JOptionPane.showMessageDialog(this, "Scroll of Aergia applied to connection!", 
                    "Scroll Applied", JOptionPane.INFORMATION_MESSAGE);
                repaint();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Click on a connection to apply Scroll of Aergia!", 
            "Invalid Target", JOptionPane.WARNING_MESSAGE);
    }
    
    private void handleScrollSisyphusClick(MouseEvent e) {
        for (INode node : state.getNodes()) {
            if (node.isOverNode(e.getX(), e.getY()) && !node.getId().equals("A") && !node.getId().equals("C")) {
                scrollSisyphusMode = false;
                JOptionPane.showMessageDialog(this, "Scroll of Sisyphus: Drag the system to move it within radius!", 
                    "Scroll Applied", JOptionPane.INFORMATION_MESSAGE);
                startNodeDragging(node, e);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Click on a non-reference system to move it!", 
            "Invalid Target", JOptionPane.WARNING_MESSAGE);
    }
    
    private void handleScrollEliphasClick(MouseEvent e) {
        for (IConnection conn : state.getConnections()) {
            if (conn.isNearLine(e.getX(), e.getY())) {
                String connectionId = getConnectionId(conn);
                state.addScrollEliphasEffect(connectionId, System.currentTimeMillis());
                scrollEliphasMode = false;
                JOptionPane.showMessageDialog(this, "Scroll of Eliphas applied to connection!", 
                    "Scroll Applied", JOptionPane.INFORMATION_MESSAGE);
                repaint();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Click on a connection to apply Scroll of Eliphas!", 
            "Invalid Target", JOptionPane.WARNING_MESSAGE);
    }
    
    private String getConnectionId(IConnection conn) {
        return conn.getFromNode().getId() + "_" + conn.getToNode().getId() + 
               "_" + conn.getFromPort() + "_" + conn.getToPort();
    }

    private void sendGameResult() {
        GameStateData finalState = convertToGameStateData();
        client.sendGameResult(finalState);
    }

    private GameStateData convertToGameStateData() {
        GameStateData data = new GameStateData();
        data.setLevel(getState().getLevel());
        data.setGameOver(getState().isGameOver());
        data.setSuccessfully(getState().isSuccessfully());
        data.setPacketLoss(getState().getPacketLoss());
        data.setLevelStartTime(getState().getLevelStartTime());
        data.setUsername(getState().getUser().getUsername());
        data.setCoins(getState().getUser().getCoin());
        data.setMaxLevelPass(getState().getUser().getMaxLevelPass());

        return data;
    }
}
