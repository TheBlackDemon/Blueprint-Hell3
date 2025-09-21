package Game;

public interface IConnectionValidator {
    boolean isValid(IConnection connection);

    boolean isCorrectPath(GameState state, StringBuilder errorMessage);
}
