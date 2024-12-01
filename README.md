# Enhanced-ABAP-Editor
An Editor plug-in for the Eclipse IDE to provide better syntax highlighting for the ABAP programming language.

# Added Token-Based Highlighting
- ✔️ Functions / Methods
- ✔️ Identifiers
- ✔️ Keywords
- ✔️ Fields
- ✔️ Comments
- ✔️ Delimiters
- ✔️ Operators
- ✔️ Literals
- ❌ Tables
- ❌ Structs (Currently treated as type)

# How does it work?
The plug-in attaches to `partActivated` to intercept Editors being opened. If the opened Editor is one of the supported ADT Editors, 
its `PresentatonReconciler` is obtained and uninstalled, then replaced with the custom Reconciler.

This method allows the plug-in to rely on SAP's ABAP Development Tools for heavy lifting, focusing only on modifying the syntax highlighting.

As of now, the Syntax Highlighter is entirely text-context-based. It's unable to look things up on the DDIC or interact with the ADT
backend in a more advanced way. This implies that any time a line is changed, the `AbapDamageRepairer` will repair (rescan) all tokens from
the previous statement terminator (in abap, this is represented by a dot `.`) to the next one, in order to ensure required context is available.

