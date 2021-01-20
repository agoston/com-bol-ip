[![Maven Central](https://img.shields.io/maven-central/v/com.bol/ip-resource.svg)](http://search.maven.org/#search%7Cga%7C1%7Ccom.bol)

# IP Resource origins

Originally developed during my work at [RIPE NCC](http://ripe.net), we needed an IP library for the [WHOIS server](http://github.com/RIPE-NCC/whois) that

* was high performance
* had minimal memory footprint
* has no dependencies
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
            <version>1.4.5</version>
        </dependency>
```

# Examples

Ipv4Resource and Ipv6Resource objects are immutable.

To parse an IPv4 interval:
```java
        Ipv4Interval myRange = IpInterval.parse("192.168/19")
```

To work with ranges/interval:
```java
        myRange.contains(IpInterval.parse("10/8"));            // false
        myRange.contains(IpInterval.parse("192.168.1.1");      // true
        myRange.contains(IpInterval.parse("2001::");           // false
        myRange.intersects(IpInterval.parse("192.168.7.0 - 192.168.7.7"));    // true
```

To use highly efficient IP interval trees for access control:
```java
        [...]
        // create & populate whitelist/blacklist tree
        NestedIntervalMap<Ipv4Interval, Boolean> map = new NestedIntervalMap<>();
        map.put(Ipv4Interval.parse("192.168/19"), true);
        map.put(Ipv4Interval.parse("192.168.52.1"), false);
        map.put(Ipv4Interval.parse("0/0"), false);

        // lookup if incoming IP is allowed to connect
        boolean allow = map.findFirstLessSpecific(new Ipv4Interval(incomingSocket.getInetAddress()));
        [...]
```
The above code would first build an IP tree where default is not allowed to connect (`0/0` has value `false`); range `192.168/19` is allowed (`true`), but then  inside that range, `192.168.52.1` is excluded once more (`false`). Then we use the `findFirstLessSpecific()` method of the tree on a connecting client's IP address to find the best match for its IP.

Of course this is just a small example, there is full IPv6 support & a lot more hierarchical lookup support, feel free to peek inside!
