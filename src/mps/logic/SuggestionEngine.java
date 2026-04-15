package mps.logic;

import java.util.ArrayList;
import java.util.List;

public class SuggestionEngine {

	public List<String> suggestionsFor(String errorCode) {
		List<String> suggestions = new ArrayList<>();
		if (errorCode == null) {
			return suggestions;
		}

		switch (errorCode) {
			case "EMPTY":
				suggestions.add("Type a valid infix expression, e.g., (3+4)*2.");
				break;
			case "UNMATCHED_PARENTHESIS":
				suggestions.add("Ensure each opening bracket has a matching closing bracket.");
				suggestions.add("Check (), {}, and [] for missing or extra brackets.");
				break;
			case "MISMATCHED_BRACKETS":
				suggestions.add("Use matching pairs: (), {}, [] in the correct order.");
				suggestions.add("Close the most recent opening bracket first.");
				break;
			case "INVALID_TOKEN":
				suggestions.add("Use only numbers and operators: +, -, *, /, ^, parentheses.");
				break;
			case "OPERATOR_MISPLACED":
				suggestions.add("Check for consecutive operators or an operator at the start/end.");
				suggestions.add("Every operator needs a number on both sides.");
				break;
			case "MISSING_OPERATOR":
				suggestions.add("Add an operator between numbers or between ')' and '('.");
				break;
			case "MISSING_OPERAND":
				suggestions.add("Add a number after an operator or before ')'.");
				break;
			default:
				suggestions.add("Check the expression syntax and try again.");
				break;
		}

		return suggestions;
	}
}
