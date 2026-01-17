# FiniFactory
### A factory game written for the J2ME platform
## How to play
Mine minerals, process them and sell for profit
## Installation
Download the latest release and install it onto device

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
