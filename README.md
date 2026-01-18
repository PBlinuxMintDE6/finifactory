# FiniFactory
### A factory game written for the J2ME platform
## How to play
Use the directional pad to navigate the cursor and use the center button to open a list of tile options. Construct opens a menu of buildings you can build. Destroy destroys the building the cursor is currently on. Rotate rotates the building the cursor is currently on by 90 degrees clockwise. Configure opens a list of recipes for the currently selected building.
## Installation
Download the latest release and install it onto device

Instructions for J2ME-Loader on Android:
- Download latest release .zip file
- Unzip the files into a known folder (recommend Downloads)
- Open J2ME-Loader and press the add option
- Navigate the the folder where the files are and select FiniFactory.jar

Requirements:
- XML API (JSR 280)
- CLDC-1.1
- MIDP-2.0
- A screen of at least 100x100
## How to mod XML file
### Buildings
A building must have:
- Name
- Description
- Logic tag (machine)
- Cost
- Stack limit
- Image path (if unsure use finifactory/invalid.png)
- Input size (defines the amount of different items can enter a building)
- Output size (defines the amount of different items can exit a building)
- Collection of recipes
### Recipes
A recipe is defined as follows:
- Recipe ID
- Time
- Collection of inputs
- Collection of outputs
### Items
An item is defined as follows:
- Item ID
- Name
- Description
- Sell price
