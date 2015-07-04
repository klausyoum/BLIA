BLIA
====

Developers generally trace the location of buggy source files based on contents in bug reports. Contents of bug reports include reported date, scenario when error case occurs, product version and other fields. When an exception occurs, the user submits a bug report with stack traces. Developers grasp the bug scenario then find the suspicious source files, which include the main words in the bug report. Stack traces are a critical clue to localizing buggy source files and lines. The code line can be traced by finding the method names or class names in the bug report. In addition, similar bug reports, which have been fixed, are also good hints for finding defects. If developers find similar bug reports in a bug/issue management system, the fixed files of similar bug reports are suspicious candidates for a new bug report. Developers search commit logs as source code change history in a SCM system to find related recent changes to affect new bugs. Analyzable inputs to improve accuracy for IR-based bug localization as follows:
* Bug report (Reported date, Scenario, Stack traces and etc.)
* Source files
* Similar fixed bug reports
* Source code change history (Commit message)

BLIA is a statically integrated analysis approach of IR-based bug localization by utilizing texts and stack traces in bug reports, structured information of source files, and source code change histories. We developed our technique based on above analyzable factors from the process for handling bug reports and from previous approaches. It is a prototyping tool for research on locating the suspicious source code files that need to be fixed in order to fix a bug.

Researchers who are interested in our approach to improve IR-based bug localization can access all datasets, results and our BLIA tool at the following link:
https://github.com/klausyoum/BLIA