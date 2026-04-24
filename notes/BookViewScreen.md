# Open BookViewScreen.java


### At line 36
EMPTY ACCESS is defined as having no pages and having no text.

### At line 318
BookAccess get an access value based on the item type. It seems to be only relevant for written book items and returns `EMPTY_ACCESS` otherwise.

It seems to be apparent to implement `BookViewScreen.BookAccess` to make our own type of 'book', as there are only vanilla definitions available. (Citing line 337, 359)

### At line 109
The init function is overridden as required. It creates the menu controls and page control buttons.

### At line 113
`createMenuControls()` creates the done button.

### At line 119
`createPageControlButtons()` 

### At line 58
PageButton extends Button.

### At line 190
`render` call.
It renders the darkened background.
It forces the Book texture - we cannot call this as super.

### At line 198
This line indicates the page number i.e "Page x of y". This is set in the LanguageProvider via
```add("whatever component", "Page %s of %s");```

