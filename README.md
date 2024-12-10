# ABAP Highlighter
A lightweight plug-in for [Eclipse](eclipse) that works with and modifies the [ADT Plugin](ADT) to provide better syntax highlighting for the [ABAP](ABAP) programming language.
It aims to address the highly dissatisfactory ADT syntax highlighting, which groups together tokens with vastly different meanings, to make looking at ABAP code slightly
more bearable.

# Supported Token Types
- ✔️ Functions / Methods
- ✔️ Identifiers
- ✔️ Keywords
- ✔️ Fields
- ✔️ Comments
- ✔️ Delimiters
- ✔️ Operators
- ✔️ Literals
- ✔️ Table Keys
- ❌ Tables *

\* Currently treated as type or identifier, depending on the context. Impossible to derive outside selection without DDIC lookup.

# How does it work?
The plugin checks for active ADT Editors and obtains & uninstalls its `PresentationReconciler`, the component that is responsible
for repairing text changes and (re-)creating color tokens based on the contents.

This way, the plug-in can rely on SAP's ABAP Development Tools for the heavy lifting, focusing only on improving the visual aspect.

As of now, the Syntax Highlighter is entirely text-context-based. It can only derive the meaning of words based on what tokens it has already found
and, occassionally, by checking the next few tokens. If the context required to derive the meaning of an identifier is not located in the statement
scope itself, much less in the active module, the scanner is unable to look up information about it. This this makes it very fast 
(most of the content is scanned in O(n) time) but limits its ability to highlight certain tokens correctly.

# Todo:
- Find a way to integrate with ADT to perform DDIC lookups
- Create & attach an ABAP file containing most syntax for testing & checking colors
- Add automated testing based on the file mentioned above, resulting token generation must be dumped.
- Make the colors (easily) customizable, currently hard coded.

Contributions are very much welcome :)
  
[ADT]: https://developers.sap.com/tutorials/abap-install-adt..html
[ABAP]: https://en.wikipedia.org/wiki/ABAP
[eclipse]: https://eclipseide.org/
