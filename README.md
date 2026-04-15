# Mathematical Problem Solver

A lightweight JavaFX desktop app that validates infix expressions, converts them to postfix (RPN) using a stack, evaluates them step-by-step, and shows helpful correction hints for common syntax mistakes.

## Features
- Infix validation (operators, operands, (), {}, [] bracket pairing).
- Infix → Postfix conversion using direct stacks.
- Step log shows push/pop stack operations.
- Suggestions for bracket and operator/operand mistakes.

## Project Structure
- `src/mps/MainApp.java` — JavaFX GUI.
- `src/mps/ConsoleRunner.java` — small CLI runner for quick tests.
- `src/mps/logic/*` — stack-based parser and evaluator.

## Run (IntelliJ IDEA)
1. Create a JavaFX run configuration for `mps.MainApp`.
2. Add VM options (adjust to your JavaFX SDK path):
   ```
   --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls
   ```

## Run (Command Line)
Set your JavaFX SDK path first, then run:
```powershell
$env:JAVA_FX_LIB="C:\path\to\javafx-sdk\lib"
$src="c:\Users\MUBASHIR KAZI\IdeaProjects\Mathematical Problem Solver\src"
$bin="c:\Users\MUBASHIR KAZI\IdeaProjects\Mathematical Problem Solver\out"

Get-ChildItem -Recurse -Filter *.java $src | ForEach-Object { $_.FullName } | Set-Content files.txt
javac --module-path $env:JAVA_FX_LIB --add-modules javafx.controls -d $bin @files.txt
java --module-path $env:JAVA_FX_LIB --add-modules javafx.controls -cp $bin mps.MainApp
```

## Quick CLI Test (No JavaFX needed)
```powershell
$src="c:\Users\MUBASHIR KAZI\IdeaProjects\Mathematical Problem Solver\src"
$bin="c:\Users\MUBASHIR KAZI\IdeaProjects\Mathematical Problem Solver\out"
Get-ChildItem -Recurse -Filter *.java $src | Where-Object { $_.FullName -notmatch 'MainApp' } | ForEach-Object { $_.FullName } | Set-Content files.txt
javac -d $bin @files.txt
java -cp $bin mps.ConsoleRunner "(3+4)*2^2"
```

## Example Inputs
- `(3+4)*2^2`
- `5 + (6 - 2) * 3`
- `7 / (2 + 5)`

## Common Errors Detected
- Unmatched parentheses: `3 + (4 * 2`
- Consecutive operators: `5 ++ 2`
- Missing operator: `2(3+4)`
