Copyright (c) 2013 Aaron Kunze (boilerpdx@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.



AndroidLocalTodo
================

This is a very simple To-do list application that was born out of the 
frustration that the "Astrid" service was shutting down.  It has a very 
minimal set of features that I needed to manage my daily life.  It was also
a nice excuse to get more familiar with Android programming.

Although I was an Astrid user, I prefer using my own storage.  So this
app uses an sqlite database on the Android device to store the tasks
database.

I wrote a simple Astrid importer for others who may have saved their
Astrid data and would like to import it.  Just put the zip file downloaded
from Astrid in the AstridImport directory on the external storage of the
device (as a peer to the "Music" directory.)

To mark a task "done", touch and hold a task in the list.

I've only tried it on the HTC One.

Feel free to submit pull requests or bug reports.

Here's my current list of features to add:
- Deleting tasks
- Backing up the database
- Lock screen widget

Enjoy!

