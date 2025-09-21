package Game;

public class NodeFactory {
    public static INode createNode(String type, int x, int y) {
        if (type.equals("A")){
            return new Node(x, y, "A", new String[]{}, new String[]{"triangle", "square"});
        } else if (type.equals("C")) {
            return new Node(x, y, "C", new String[]{"triangle", "square"}, new String[]{});
        } else if (type.startsWith("spy_")) {
            return new Node(x, y, type, new String[]{"triangle", "square"}, new String[]{"triangle", "square"}, "spy");
        } else if (type.startsWith("sabotage_")) {
            return new Node(x, y, type, new String[]{"triangle", "square"}, new String[]{"triangle", "square"}, "sabotage");
        } else if (type.startsWith("vpn_")) {
            return new Node(x, y, type, new String[]{"triangle", "square"}, new String[]{"triangle", "square"}, "vpn");
        } else if (type.startsWith("antitrojan_")) {
            return new Node(x, y, type, new String[]{"triangle", "square"}, new String[]{"triangle", "square"}, "antitrojan");
        } else {
            return new Node(x, y, type, new String[]{"triangle", "square"}, new String[]{"triangle", "square"});
        }
    }
    
    public static INode createNode(String type, int x, int y, String systemType) {
        if (type.equals("A")){
            return new Node(x, y, "A", new String[]{}, new String[]{"triangle", "square"});
        } else if (type.equals("C")) {
            return new Node(x, y, "C", new String[]{"triangle", "square"}, new String[]{});
        } else {
            return new Node(x, y, type, new String[]{"triangle", "square"}, new String[]{"triangle", "square"}, systemType);
        }
    }

    public static INode createNodeWithShapes(String id, int x, int y, String[] outputShapes, String[] inputShapes, String systemType) {
        String sys = (systemType == null || systemType.isEmpty()) ? "normal" : systemType;

        if ("A".equals(id)) {
            String[] enforcedOutputs = (outputShapes == null || outputShapes.length == 0)
                    ? new String[]{"triangle", "square"}
                    : outputShapes;
            return new Node(x, y, id, new String[]{}, enforcedOutputs, sys);
        }
        if ("C".equals(id)) {
            String[] enforcedInputs = (inputShapes == null || inputShapes.length == 0)
                    ? new String[]{"triangle", "square"}
                    : inputShapes;
            return new Node(x, y, id, enforcedInputs, new String[]{}, sys);
        }

        String[] outShapes = (outputShapes == null) ? new String[]{} : outputShapes;
        String[] inShapes = (inputShapes == null) ? new String[]{} : inputShapes;
        return new Node(x, y, id, inShapes, outShapes, sys);
    }
}
