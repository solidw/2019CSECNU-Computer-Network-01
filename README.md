# Computer-Network-Term-Project

##### ARP cash

```
Interface : String
IP Address : Byte[4]
Ethernet Address : Byte[6]
Status : Boolean
ARP Layer에 static filed로 설정
```

##### Proxy ARP

```
Device Name : String
IP Address : Byte[4]
Ethernet Address : Byte[6]
```



### Header

#####    IP Header ( 20 Byte )

    Version : 4 bit (4로 고정)
    Header LengthHLEN : 4 bits
    Type Of Service : 1 Byte
    Total Packet Length : 2 Byte
    Fragment Identifier : 2 Byte
    Fragmentation Flag : 3 bits
    Fragmentation Offset 13 bits
    TTL : 1 Byte
    Protocol Identifier : 1 Byte
    헤더 체크섬 : 2 Byte
    Source IP : 4 Byte (12 번째 부터)
    Destination IP : 4 Byte (16 번째 부터)

#####    Ethernet Header (14 Byte)

    Destination : 6byte
    Source : 6Byte
    Type : 2Byte

#####    ARP Header (28 Byte)

    HW Type : 2 Byte
    Protocol : 2 Byte
    HW Length : 1 Byte
    Protocol Length : 1 Byte
    Opcode : 2 Byte
    Sender Mac : 6 Byte
    Sender IP : 4 Byte
    Target Mac : 6 Byte
    Target IP : 4 Byte

2분 동안 Wait, 응답 없으면 캐시 지우기
2분안에 응답이오면 10분동안 캐시 저장

![1570346289157](C:\Users\solidw\AppData\Roaming\Typora\typora-user-images\1570346289157.png)