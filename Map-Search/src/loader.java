import java.io.IOException;

public class loader {
 public static void main(String[] args) throws IOException {
  int nanimals = 30;
  for (int i=0; i<10000; i++) {
   Simulator.main(new String[] { "MyNaturalist", "--seed=" + i,
     "--nanimals="+nanimals, "--ntrees=35", "--headless"});
  }
 }
}