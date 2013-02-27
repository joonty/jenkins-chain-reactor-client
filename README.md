## Chain Reactor Client for Jenkins
### Notify other machines about Jenkins build statuses

[Chain Reactor][1] is a server written in ruby that listens for events that are fired by clients. This plugin is a client for Jenkins that sends build information after builds complete to one or many chain reactor servers that you set up. This means that other machines can respond to a build failure/success.

### Installation 

By far the easiest way to install this plugin is through the Jenkins update center. However, you can download an HPI file from the downloads section if you would prefer to install it manually.

### Configuration

Go to the system configuration page (Manage Jenkins -> Configure System), and scroll down to "Global Chain Reactor Settings". You can add multiple IP addresses here, corresponding to the computers that are running the chain reactor server.

### Building

If you want to build this package you'll need to have JDK 6 and maven3 installed, and the installation process depends on your OS and package manager.

Clone the git repository and build the package

    git clone https://github.com/joonty/jenkins-chain-reactor-client.git 
    cd jenkins-chain-reactor-client
    mvn package

This will generate an hpi file at `target/chainreactorclient.hpi`. This needs to be copied to the Jenkins plugin directory. If a version of this plugin has already been installed, run

    rm -rf /var/lib/jenkins/plugins/chainreactorclient*

to get rid of it. Then either use the advanced tab of the plugin manager to upload the hpi file or copy it to the plugins directory, e.g. 

    cp target/chainreactorclient.hpi /var/lib/jenkins/plugins/

Finally, restart jenkins.

### License

Copyright &copy; 2012, Jonathan Cairns. Licensed under the [MIT license].

[1]: https://github.com/joonty/chain-reactor
