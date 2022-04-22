# bots
An experiment in artificial life, artificial neural nets, artificial sentience, simulated evolution, simulated consciousness, and genetic programming

## Requirements

A linux, mac, or unix equivalent command line and the 'sh' shell.
- If you are on a Mac, and don't know how to access the terminal, see [Opening a Terminal on a Mac](https://support.apple.com/guide/terminal/open-or-quit-terminal-apd5265185d-f365-44cb-8b09-71a064a42125/mac).
- I you are on Linux, you can usually open Terminal by pressing: Ctrl+Alt+T

A graphical user interface to display UI. If you are seeing this from a graphical web browser then you are all set.

You must have the [Java Development Kit](https://openjdk.java.net/install/) to compile the software.

git is recommended but not required. If you do not have git installed but would like to install it, [click here](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).

## Download Instructions

If you have git installed, then from the terminal run the following command:
  
  `$ git clone "https://github.com/jasonkresch/bots.git"`

If you do not have git installed:

Download the following project zip by [clicking here](https://github.com/jasonkresch/bots/archive/refs/heads/main.zip) or by running the following command from your terminal:

  `$ wget "https://github.com/jasonkresch/bots/archive/refs/heads/main.zip"`

Extract the ZIP file and rename the extracted folder to 'bots' for consistency with the rest of the instructions.

  `$ unzip main.zip`
  
After extracting, it will create a folder 'bots-main' which is different from the folder created using git. For consistency with the rest of the instructions below it is best to rename this folder to 'bots' as follows:
  
  `$ mv "bots-main" "bots"`


## Compilation Instructions

After downloading the project, run the following commands from your command line:

```
$ cd "bots/artificial-life/"
$ ./build.sh
```

Note that the java compiler (javac) is required to be on your system. If you do not have it, download and install the appopriate Java Development Kit (JDK) for your system. See: https://openjdk.java.net/install/ for more information if you get 'javac: command not found' or any similar error message from running the build command.

## Running Instructions

To start the program run the following command from the 'bots/artificial-life/ directory:

`$ ./run.sh`

This will bring up the main control window user interface. This interface will allow you to set various parameters of the artificial life simulation.

Once you have chosen the parameters and are ready to begin, click 'Start Training'. This will begin the process of evolving ever more capable bots. After training has started, you may, at any time click the 'Show Bot Arena' button to keep an eye on how the bots are performing in the latest generation.

When the "Enable Autosave" option is checked, the progress will be saved to "autosave.bot" in the "saves" directory every 10 generations. To continue where you left off, click the "Load State" button. To prevent your settings from being overwritten by an autosave, click the "Save State" button to save the progress to a location and file name of your choice.

*Not that once the training is started, Generation Size and Bot Brain Size can no longer be adjusted.* Please choose these carefully before starting a new evolution experiment! See the section below for more information about the meaning of each of these settings.

## Simulation Parameters

The following is a detailed description of the meaning of each of each of the configurable parameters of the simulation.

### Evolution Parameters

* **Generation Size** - The number of bots that compete against each other in each iteration of evolution. The top performing bots from this generation are selected to pass their genes (and brains) on to the next generation of bots.
* **Fraction that Survive** - The percentage of bots that survive to the next generation. The worst performing bots that do not survive are removed and replaced with bots created from reproduction and mutation of the best performing bots.
* **Generation Time** - The number of time steps which compose each generation. The longer this goes the more likly the best performing bots will rise to the top.
* **Mutation Rate** - Controls the fraction of genes that are randomly mutated (replaced with random values) or tweaked (adjusted up or down slightly) for bots that are created through mutation.

### Bot Parameters

* **Bot Brain Size** - The number of neurons in the hidden layer of the Bot's neural network brain, which sit between the bot's 22 input neurons, and 4 output motor neurons which control the bots turning, movement, antenna angle, and antenna length.
* **Max Bot Turn Rate** - The maximum number of degrees per time step at which a bot can turn either right or left.
* **Max Bot Speed** - The maximum percentage of the arena the bot can traverse per time step.
* **Bot Field of View** - The number of degrees across which the bot can adjust the position of its antenna.
* **Bot Antenna Length** - The fraction of the course across which the Bot can extend its antenna.

### Environment Parameters

* **Green Balls** - The number of green (good) balls in the environment for the bots to "feed" on and increase their fitness (chance of passing their genes on. The least fit of each generation are replaced.
* **Red Balls** - The number of red (bad) balls in the environment which harm the bots when their antenna touch them. The more a bot touches a red ball, the more the bot's fitness will decrease, and therefore makes it less likely to survive or reproduce.
* **Ball Resets Per Generation** - The number of times during each generation the position and velocity of all balls in the environment will randomly change. This is meant to prevent lucky or unintelligent bots from being rewarded by fortunate placement near a green ball.
* **Green Benefit** - The amount of relative benefit given to each bot for contacting a green ball with its antenna. Note that when this is the same value as the red detriment there is no difference in outcome for the bots. Only relative differences between the green benefit and red detriment should have an impact on evolved behaviors.
* **Red Detriment** - The amount of relative detriment each bot is punished for contact with a red ball.
* **Ball Size** - The size of each ball, representing the ball diammeter as a percentage of the total arena size.
* **Solid Walls** - When checked, balls bounce, and bots get stuck. When unchecked, balls and bots will 'reflect' to the opposite side of the arena.

### Display Parameters

* **Bots to Show** - The number of bots to show on screen to monitor the progress of the latest generation of bots.
* **Frames Per Second** - How fast to update the animation of the bots on screen, in terms of number of time steps per second. Note that changing this has no impact on the speed of training.
* **Time to Refresh** - Number of seconds to display bots before updating state from the latest generation of training.


