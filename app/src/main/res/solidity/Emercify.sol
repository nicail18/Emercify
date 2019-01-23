pragma solidity ^0.4.25;

contract Emercify{

    struct User{
        string user_id;
        string name;
        string username;
        string email;
        uint fakeReports;
        uint legitReports;
        bool set;
    }

    struct EmergencyPost{
        string post_id;
        string user_id;
        string caption;
        bool isLegit;
    }

    User private tmp;
    mapping (string => User) users;
    mapping (string => EmergencyPost) emergencyPosts;
    string[] user_ids;
    string[] posts_ids;

    function _setUser(string _user_id, string _name, string _username, string _email) public{

            User storage user = users[_user_id];
            if(!user.set){
                user.user_id = _user_id;
                user.name = _name;
                user.username = _username;
                user.email = _email;
                user.set = true;
                user.fakeReports = 0;
                user.legitReports = 0;

                user_ids.push(_user_id) -1;
            }
            else{
                 _editUser(_user_id,_name,_username,_email);
            }
    }

    function _editUser(string _user_id, string _name, string _username, string _email) public{
        users[_user_id].name = _name;
        users[_user_id].username = _username;
        users[_user_id].email = _email;
    }

    function _userCount() view public returns (uint){
        return user_ids.length;
    }

    function _getUser(string _address) view public returns (string, string, string, string, bool){
        return(users[_address].user_id,users[_address].name,users[_address].username,users[_address].email,users[_address].set);
    }

    function _setEmergencyPost(string _post_id, string _user_id, string _caption) public {
        EmergencyPost storage temp = emergencyPosts[_post_id];
        temp.post_id = _post_id;
        temp.user_id = _user_id;
        temp.caption = _caption;
        temp.isLegit = false;

        posts_ids.push(_post_id) -1;
    }

    function reportPost(string _user_id, string _post_id,  bool isFake) public {
        if(!isFake){
            emergencyPosts[_post_id].isLegit = false;
            users[_user_id].fakeReports++;
        }
        else{
            emergencyPosts[_post_id].isLegit = true;
            users[_user_id].legitReports++;
        }
    }

    function _editUsername(string _user_id, string _username) public {
        users[_user_id].username = _username;
    }

    function _editName(string _user_id, string _name) public {
        users[_user_id].name = _name;
    }

    function getUserReports(string _user_id) view public returns (string, uint, uint){
        return (_user_id, users[_user_id].fakeReports,users[_user_id].legitReports);

    }

}