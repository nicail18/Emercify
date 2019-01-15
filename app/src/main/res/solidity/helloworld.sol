pragma solidity ^0.4.25;

contract MessageContract {
    string message = "Hello World";

    function getMessage() public constant returns(string) {
        return message;
    }

    function setMessage(string newMessage) public {
        message = newMessage;
    }
}