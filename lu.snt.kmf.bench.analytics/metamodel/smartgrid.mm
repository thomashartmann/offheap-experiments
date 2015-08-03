kmfVersion "4.19.0"
version "1.0.0-SNAPSHOT"

class sample.Cloud {
    ref* nodes : sample.Node
}
class sample.Node {
    att name : String
    ref* softwares : sample.Software
}
class sample.Software {
    att name : String
    att size : Int
}
