Lifty 1.7
=========

This is the branch for version 1.7 of Lifty. I've learned a lot the past year and this branch is where I try to convert these ideas into code. The following are the features that I'm working on: 

1. Switching to SBT 0.9.x plugins instead of processors.

2. **Recipes**: A set of lifty templates together with a description.json file is now knows a recipe (if you have a better word, let me know). Lifty comes with the Lift recipe by default but other recipes can be added by running "lifty learn http://my.url.scalatest.json" and you get templates for scalatest files (if such a lifty recipe existed). This also means that the description.json file and the templates are now hosted online so default values of arguments and the templates can be updated without having to reinstall lifty. 


