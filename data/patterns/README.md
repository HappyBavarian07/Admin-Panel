Pattern files for validation

- Place generated pattern files in this folder.
- Pattern values use the form `REGEX:<pattern>` to validate values.
- Keys may be either literal keys or regex keys; regex keys will be matched against data keys.

How to generate:

- Use `PatternFileGenerator.generateYamlPattern(inputFile, outputPatternFile)`.
- Optionally provide an overrides map that maps key-path regex to a desired pattern string.

How to validate:

- Use `PatternValidator.validateYaml(dataFile, patternFile)` which returns a ValidationResult with lists of
  missing/unexpected/mismatched entries.

