public class Tmp2 {

    public String getPasswordHash(User user, HashFunction hasher) {
        String password = user.getPassword();
        String salt = user.getSalt();
        String input = password + salt;
        String hash = hasher.hash(input);
        return hash;
    }

}