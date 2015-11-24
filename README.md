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
            <groupId>com.bol</groupId>
            <artifactId>ip-resource</artifactId>
            <version>1.0</version>
        </dependency>
```

# Examples

Ipv4Resource and Ipv6Resource objects are immutable.

To parse an IPv4 interval:
```java
        Ipv4Resource myRange = Ipv4Resource.parse("192.168/19")
```

To work with ranges/interval:
```java
        myRange.contains(Ipv4Resource.parse("10/8"));            // false
        myRange.contains(Ipv4Resource.parse("192.168.1.1");      // true
        myRange.intersects(Ipv4Resource.parse("192.168.7.0 - 192.168.7.7"));    // true
```

To use highly efficient IP interval trees:
```java
        [...]
        // create & populate whitelist/blacklist tree
        NestedIntervalMap<Ipv4Resource, Boolean> map = new NestedIntervalMap<>();
        map.put(Ipv4Resource.parse("192.168/19"), true);
        map.put(Ipv4Resource.parse("192.168.52.1"), false);
        map.put(Ipv4Resource.parse("0/0"), false);

        // lookup if incoming IP is allowed to connect
        boolean allow = map.findFirstLessSpecific(new Ipv4Resource(new Socket().getInetAddress()));
        [...]
```
