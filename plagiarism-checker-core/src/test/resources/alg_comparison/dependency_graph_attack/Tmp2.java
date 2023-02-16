public class Tmp2 {

    public String getPasswordHash(User user, HashFunction hasher) {
        String tmpPass = user.getPassword();
        String password = tmpPass;
        String salt = user.getSalt();
        String tmpInput = password + salt;
        String input = tmpInput;
        String hash = hasher.hash(input);
        return hash;
    }

}