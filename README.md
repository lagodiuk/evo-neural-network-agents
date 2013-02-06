evo-neural-network-agents
=========================

Evolving of neural networks for collective agents

More info in this [article](http://habrahabr.ru/post/168067/) (in Russian)

This project depends on [Generic Genetic Algorithm project](http://github.com/lagodiuk/genetic-algorithm) (has a maven dependency)

## About ##

### basic concepts ###
How neural network driven agent interacts with environment:
![Agent in environment](https://raw.github.com/lagodiuk/evo-neural-network-agents/master/about/agent.png)

### demonstrations on Youtube ###
Evolving of neural network driven agents
* [video 1](http://www.youtube.com/watch?v=QV1EML_BWDc)
* [video 2](http://www.youtube.com/watch?v=fxOeVCZmc1Y)
* [video 3](http://www.youtube.com/watch?v=hDnLhehf4lU)
* [video 4](http://www.youtube.com/watch?v=3nZETrsCMgw)


### try it ###
* Install [Java Runtime Environment](http://www.java.com/en/download/help/download_options.xml)
* Download <i>simulator.jar</i> from http://github.com/lagodiuk/evo-neural-network-agents/tree/master/bin
* Launch simulator from command line: <i>java -jar simulator.jar</i>
* You can find [here](http://github.com/lagodiuk/evo-neural-network-agents/tree/master/brains) different evolved configurations of neural networks


### for developers ###
Language: Java <br/>
Build with: Maven <br/>
<ol>
<li> git clone https://github.com/lagodiuk/genetic-algorithm.git </li>
<li> git clone https://github.com/lagodiuk/evo-neural-network-agents.git </li>
<li> mvn -f genetic-algorithm/pom.xml install </li>
<li> mvn -f evo-neural-network-agents/pom.xml install </li>
</ol>

Architecture of principal components of application:
![Architecture](https://raw.github.com/lagodiuk/evo-neural-network-agents/master/about/architecture.png)

### evolving neural networks ###
How neural networks evolving with genetic algorithm:
![Neural network](https://raw.github.com/lagodiuk/evo-neural-network-agents/master/about/nn_ga.png)