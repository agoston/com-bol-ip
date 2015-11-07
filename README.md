# IP Resource origins

Originally developed during my work at [RIPE NCC](http://ripe.net), we needed an IP library for the [WHOIS server](http://github.com/RIPE-NCC/whois) that

* was high performance
* had minimal memory footprint
* supported addresses, ranges, prefixes
* supported contains/intersect operations
* had a natural tree support
* supported IPv4 and IPv6 in a protocol-independent manner
* supported in-addr.arpa/ip6.arpa (a.k.a. reverse domain) format

While this was working well, this little gem was never released in public. Now I've removed the whois-specific parts and packaged it for maven central.

# License

Inherited the BSD license of the original project.

# Usage

Add the following to `pom.xml`:

```
        <dependency>
            <groupId>com.bol.ip</groupId>
            <artifactId>ip-resource</artifactId>
            <version>1.0</version>
        </dependency>
```

# Examples

Coming soon
