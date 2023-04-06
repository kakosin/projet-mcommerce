# Welcome to MCommerce docs contributing guide

We're using the [Code of Conduct](./CODE_OF_CONDUCT.md) from GitHub to keep our community approachable and respectable.

## Naming standards
- Names must conform to the rules:

### Encoding:
- Source files must be encoded in UTF-8

### Package:
- Package names should be limited to one word per segment/subfolder and be lowercase
- They should be simple, descriptive

### Classes, interfaces:
- Names must start with a capital letter. All other letters are lowercase, unaccented
- Names should be simple, descriptive and pronounceable
- Acronyms and abbreviations should be avoided as much as possible.
- The first word of the name must be a name
- Class names that implement an interface must end with the suffix Impl
- A class that extends Exception must end with the suffix Exception
- Abstract classes must start with the prefix Abstract
- Generic terms should be avoided.

### Methods :
- The name of a method must begin with an uppercase letter. All other letters are lowercase, unaccented
- Names should be simple, descriptive and pronounceable
- The first word of the name must be a verb or verb group in the infinitive which indicates the treatment carried out
- Avoid acronyms, abbreviations and similar names
- Generic terms should be avoided.

### Variables and attributes:
- Names must start with a capital letter. All other letters are lowercase, unaccented
- The first word of the name should indicate the role the variable or attribute should play
- Be sure to use public, final and static modifiers when declaring constants
- Avoid acronyms, abbreviations and similar names

## Pull request:
- In the case of a new feature, the pull request must be named as follows (New: description of the new feature).
- In the case of a feature modification, the pull request must be named as follows (Change: description of the new feature / change).
- In the case of a bug fix, the pull request must be named as follows (Fixed #Bogue: description of the fix).

## Coding standards:
- Use a code formatting tool (e.g. IntelliJ) in combination with team formatting file and plugins (like CheckStyle)
- A function does not exceed about 20 lines
- A function is supposed to do one thing. If the code does more than one thing, it should be separated into sub-functions.
- Each method should be written to be from a single level of abstraction
- It is recommended to have as few arguments as possible for a function.
- A function must do something or return something.
- It is recommended to extract try/catch blocks in a separate method to improve readability and reduce complexity.
- When you exceed 3 arguments or around 20 lines, you have to refactor.
- If the IDE indicates the duplication, it is necessary to take out the karcher and centralize the code.

## Writing unit tests
- Each Pull Request must contain at least one test.
- When creating new tests for a function, make sure to give a meaningful name to the test (NameFunction_Test).
- For a new feature, make sure to add at least one test for each of the following cases (test of a normal case, test that fails, test of the limit values).
- For a bug fix, in addition to the tests contained in the release, make sure to add at least one test that validates the fix to ensure that the bug is fixed.

## Bug Handling Standards:

A bug's status should be set to (in progress) the moment it is picked up by a contributor and the contributor begins work on it.
The status must be updated a second time to (completed) as soon as the Pull Request containing the correction is approved. In case the contributor decides to abandon the fix then he must return the bug status to (to do)
