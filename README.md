# ABAP Visuals
Lightweight plug-in for the Eclipse IDE that modifies [ADT](ADT) (Abap Development Tools) to provide better syntax highlighting for the [ABAP](ABAP) programming language.
It aims to solve the extremely dissatisfactory syntax highlighting that ADT supplies, as they group tokens together that have vastly different meanings. 
If you've ever programmed ABAP, you either know exactly what I'm referring to or you're blissfully unaware.

# Added Highlighting
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
The plugin looks for active ADT Editors and obtains, then uninstalls its `PresentationReconciler`, the component that is responsible
for repairing text changes and (re)creating color tokens based on the text.

That way, the plug-in can rely on SAP's ABAP Development Tools for heavy lifting, focusing only on improving the syntax highlighting.

As of now, the Syntax Highlighter is entirely text-context-based. It can only derive the meaning of words based on what tokens it has already found
and occassionally by checking the next few tokens. If the context required to derive the meaning of an identifier is not located in the statement
scope itself, much less in the active module, the highligher is unable to look up information about it. This this makes it very fast 
(most of the content is scanned in O(n) time) but limits its ability to highlight certain tokens correctly.

# Todo:
- Find a way to integrate with ADT to perform DDIC lookups
- Create & attach an ABAP file containing most syntax for testing & checking colors
- Add automated testing based on the file mentioned above, resulting token generation must be dumped.
- Make the colors (easily) customizable, currently hard coded.

Contributions are welcome.
  
[ADT]: https://developers.sap.com/tutorials/abap-install-adt..html
[ABAP]: https://en.wikipedia.org/wiki/ABAP
