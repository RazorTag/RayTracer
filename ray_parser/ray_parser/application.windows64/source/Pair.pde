class Pair {
  int[] point;
  
  Pair (int x, int y) {
    point = new int[2];
    point[0] = x;
    point[1] = y;
  }
  
  int x() {
    return point[0];
  }
  
  int y() {
    return point[1];
  }
}
