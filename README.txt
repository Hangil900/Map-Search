Treasure Hunter Program

Collaborated with Chris Jung to implement the MyNaturalist file. 
The other files were written with the help of our Professor.

To Run: After downloading all files, click Run, and in the option that comes up, choose to run the Simulator. Then click on Run Configurations, and in Simulator, go to Arguments, and change Program arguments to "MyNaturalist".
Now simply clicking run, will start the program.

In this program, an "optimal naturalist" has been implemented. The objective of the program is to collect all animals on the map and bring them back to the ship with minimized cost. The cost value was calculated by the number of nodes the naturalist has to travel while completing the objective. The cost increases whenever the naturalist carries an animal, porportional to the animals weight. A naturalist was assumed to be able to carry at most 3 animals at once.

In our implementation, the naturalist first explores the island using a Depth First Search, storing the information gathered at each node in a distance matrix, recording the animals at each node, and the weight of the animals. Then upon returning to the ship the naturalist calculates the most optimal sequence of moves using the Floyd-Warshall Algorithm, and executes this sequence of moves to minimize the cost.