public class Tmp1 {

    private String getInput(User user) {
        String password = user.getPassword();
        String salt = user.getSalt();
        String input = password + salt;
        return input;
    }

    public String getPasswordHash(User user, HashFunction hasher) {
        String input = getInput(user);
        String hash = hasher.hash(input);
        return hash;
    }

}