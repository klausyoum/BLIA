BLIA
====

Developers generally trace the location of buggy source files based on the contents of bug reports, which include the report date, scenario in which the error occurs, product version, reporter, status and other fields. When an exception occurs, the user submits a bug report with stack traces. Developers grasp the bug scenario and then find the suspicious source files, which include the main words in the bug report. Stack traces are significant for localizing buggy source files and lines. The code line can be traced by finding the method names or class names in the bug report. Comments in bug reports include additional information that was not included when submitting bug reports. They also include the stack traces or crash logs of the software program.

In addition, similar bug reports that have been fixed also assist defect localization. If developers have found similar bug reports in a bug/issue management system, the fixed files of similar bug reports are candidates for the new bug report. Developers search commit logs as a source code change history in an SCM system to find related recent changes that may have produced new bugs. They analyze file, method or line differences between two versions to determine the bug location accurately.

To summarize, analyzable inputs to improve the accuracy for IR-based bug localization as follows:

* Bug report (reported date, scenario, stack traces, comments and etc.)
* Source files
* Similar fixed bug reports
* Source code change history (commit messages and differences among changes)

BLIA is a statically integrated analysis approach of IR-based bug localization by utilizing texts and stack traces in bug reports, structured information of source files, and source code change histories. We developed our technique based on above analyzable factors from the process for handling bug reports and from previous approaches. It is a prototyping tool for research on locating the suspicious source code files and methods that need to be fixed in order to fix a bug.

Researchers who are interested in our approach to improve IR-based bug localization can access all datasets, results and our BLIA tool at the following link:

https://github.com/klausyoum/BLIA