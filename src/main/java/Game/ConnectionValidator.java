package Game;

import java.util.ArrayList;
import java.util.List;

public class ConnectionValidator implements IConnectionValidator {
    @Override
    public boolean isValid(IConnection connection) {
        if (connection.getFromNode().getId().equals(connection.getToNode().getId())) {
            System.out.println("It is not possible to establish a connection between the input and output ports of a Node . at Node :" +
                    connection.getFromNode().getId());
            return false;
        }
        if ("A".equals(connection.getToNode().getId())) {
            System.out.println("Invalid connection: Node A cannot have input ports.");
            return false;
        }
        if ("C".equals(connection.getFromNode().getId())) {
            System.out.println("Invalid connection: Node C cannot have output ports.");
            return false;
        }
        String fromShape = connection.getFromNode().getOutputShapes()[connection.getFromPort()];
        String toShape = connection.getToNode().getInputShapes()[connection.getToPort()];
        if ("confidential_6".equalsIgnoreCase(toShape)) {
            System.out.println("Invalid connection: Target input port is blocked.");
            return false;
        }
        return fromShape.equals(toShape);
    }

    @Override
    public boolean isCorrectPath(GameState state, StringBuilder errorMessage) {
        List<String> unconnectedNodes = new ArrayList<>();
        for (INode node : state.getNodes()) {
            String[] inputShapes = node.getInputShapes();
            int requiredInputs = 0;
            for (String shape : inputShapes) {
                if ("confidential_6".equalsIgnoreCase(shape)) {
                    requiredInputs++;
                }
            }
            int connectedCount = node.getConnectedInputs().size();
            if (requiredInputs > 0 && connectedCount <= requiredInputs) {
                unconnectedNodes.add(node.getId());
                System.out.println("Validation failed: Node " + node.getId() + " has unconnected inputs (" +
                        connectedCount + "/" + requiredInputs + ")");
            }
        }
        if (!unconnectedNodes.isEmpty()) {
            errorMessage.append("All nodes must be fully connected. Unconnected nodes: ")
                    .append(String.join(", ", unconnectedNodes));
            return false;
        }
        System.out.println("Validation succeeded");
        errorMessage.append("Success: Valid path ");
        return true;
    }
}
