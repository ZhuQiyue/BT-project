# BitTorrent-Java-CN5106C
Contributors
Qiyue Zhu
Rajat Rai
Lin Huang

CNT5016 Spring 2021

Demonstration

+ Parsing Common.cfg and PeerInfo.cfg
  
  + We use java.util.Properties to load files. For common.cfg, we just save this information into PeerProcess so everyone can access it; For PeerInfo.cfg, we instantiate DynamicPeerInfo when we read the file and also save it as an array in PeerProcess.

+ Establishing TCP connection and handshaking
  
  + Each peer has a peer Index, depend one their order in PeerInfo.cfg. All the Peers try to connect the peers with lower index and then listen to the specific port to wait for higher index peers to connect them. We do handshaking right after TCP connection is established.

+ Bitfield implementation
  
  + We implement bit-field as a list of boolean stored in each DynamicPeerInfo. Index i is true indicates this peer has i'th file piece. When we instantiate the DynamicPeerInfo, if this peer has a file initially, we set all boolean to 1; otherwise to 0. When we receive a bit-field/have/filePiece message, we reset these booleans according to the context of the message.

+ Sending and receiving interested/not-interested messages

  + We only send interested/not-interested messages when the state of interest is changed.
  + When we receive bitfield/have messages if there is any file piece they have but we don't, then we send an interesting message. Otherwise, we send not interested messages.
  + When we receive any file piece, we check if we are not interested in some peers anymore. If that is the case, we send not interested messages.
  + When we receive an interested/not interested message, we change the information in DynamicPeerProcess, so the DecisionMaker know who to unchock

+ Choke/unchoke/optimistic unchoking
  + We keep track of if we are choking/unchoking and if all choke us /unchoked the other peers in the array of DynamicPeerInfos
  + Each peer has k preferred neighbors and 1 optimistic unchoked neighbor, if there are enough peers makes TCP connection to it
  + We use two java.util.TimerTask in decision-maker to change preferred peers and optimistically unchoked peer. The interval of the timer is read from PeerProcess, which got from common.cfg
  + If we have a complete file, then we choose a random preferred peer. We do this by getting all the peers interested in our pieces, and shuffle them, then get the first k peers.
  + When we do not have a complete file, we sort all the other interested peers by their contribution to us during the last interval, then choose the first k peers
  + We choose optimistically unchoked peer randomly from all the peers that are choked and interested in us
  + We check if the remote peer is choked before sending any file piece, in case we send a choked message but the remote peer has not received it/processed it yet

+ Request and have messages
  + We send request messages in two cases: receive an "unchoke" and receive a filepiece. We only send it if we have at least one interesting file in the remote peer, and we have not requested it from any other peers.
  +  We send have a message to all the other peers when we receive a file piece.
  +  When receiving a request message, if the remote peer is unchoked, we send the file piece; When receiving a "have" message, we change the bit-field in DynamicPeerInfo.

+ Pieces being generated and transferred
  + We generate an empty file at the beginning with the size specified in common.cfg, if we do not have it initially.
  + We have a fileHelper to read and write file. This file Helper is implemented by java.io.RandomAccessFile.
  + When we need to generate file piece, we use the piece index to calculate the right pointer to seek the file, then read a byte array of a file piece, and finally wrap it into a file piece message.
  + Pieces are transferred as the payload of file piece message
  + When we receive a file piece message, we overwrite the specific place of the file with the byte array payload.

+ Termination of all peers (file being received by all peers)
  + We keep track of how many file piece we and other peers have received.
  + When we receive a file piece or have a message, we check if it is a good time to terminate.
  + When everyone has a complete file, we decide to terminate. Then we wait for 10 seconds in case log has not been fully written.


Demo

javac cnt5106C/PeerProcess.java

ssh lin114-00.cise.ufl.edu "cd CNT2 ; java cnt5106C.PeerProcess 1001"

ssh lin114-01.cise.ufl.edu "cd CNT2 ; java cnt5106C.PeerProcess 1002"

ssh lin114-02.cise.ufl.edu "cd CNT2 ; java cnt5106C.PeerProcess 1003" 

ssh lin114-03.cise.ufl.edu "cd CNT2 ; java cnt5106C.PeerProcess 1004" 

ssh lin114-04.cise.ufl.edu "cd CNT2 ; java cnt5106C.PeerProcess 1005"

ssh lin114-05.cise.ufl.edu "cd CNT2 ; java cnt5106C.PeerProcess 1006"



Contribution

Qiyue Zhu
+ Design the major structure of the project, including the idea of PeerProcess, priority Queue, DynamicPeerInfo, DecisionMaker, Message, Handlers and how they should interact with each other
+ Implement TCP connection establishment in PeerProcess
+ Implement dynamicPeerInfo
+ Implement Message, and its sending/receiving thread
+ Implement bitfield and handshake handler
+ Participate in debug and rewrite of some problematic functions

Rajat Rai
+ Design the DecisionMaker of the project
+ Implemented Message Type Handlers
+ Implement random Peer Selection
+ Implement Logger
+ Systems Testing 

Lin Huang
+ Implement File Handler and Transfer between hosts
+ Read Pre-exiting Config files
