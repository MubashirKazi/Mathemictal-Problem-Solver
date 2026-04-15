package mps.logic;

public class ExpressionResult {
    private final String postfix;
    private final String result;
    private final String stepsText;
    private final String errorText;

    public ExpressionResult(String postfix, String result, String stepsText, String errorText) {
        this.postfix = postfix;
        this.result = result;
        this.stepsText = stepsText;
        this.errorText = errorText;
    }

    public String getPostfix() {
        return postfix;
    }

    public String getResult() {
        return result;
    }

    public String getStepsText() {
        return stepsText;
    }

    public String getErrorText() {
        return errorText;
    }
}
