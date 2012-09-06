/*
 * The MIT License
 *
 * Copyright 2012 Jon Cairns <jon.cairns@22blue.co.uk>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.chainreactorclient;

import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Jon Cairns <jon.cairns@22blue.co.uk>
 */
public class ChainReactorServer implements Serializable {
	public int port;
	public String url;

	public ChainReactorServer() {}

	@DataBoundConstructor
	public ChainReactorServer(String url, int port) {
		this.url = url;
		this.port = port;
	}

	public String getUrl() {
		return url;
	}

	public int getPort() {
		return port;
	}

    public InetSocketAddress getSocketAddress() throws UnknownHostException {
        InetAddress addr = InetAddress.getByName(getUrl());
        return new InetSocketAddress(addr,getPort());
    }

    public String toString() {
        return url + ":" + port;
    }
}
