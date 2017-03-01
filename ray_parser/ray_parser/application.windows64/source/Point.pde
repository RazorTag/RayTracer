class Point {
  public float[] point;
  
  Point (float x, float y, float z) {
    point = new float[3];
    point[0] = x;
    point[1] = y;
    point[2] = z;
  }
  
  float x() {
    return point[0];
  }
  
  float y() {
    return point[1];
  }
  
  float z() {
    return point[2];
  }
  
  Point addPoints(Point other) {
    Point newPoint = new Point(point[0]+other.point[0],
      point[1]+other.point[1], point[2]+other.point[2]);
    return newPoint;
  }
  
  //return this - other
  Point subtractPoints(Point other) {
    Point newPoint = new Point(point[0] - other.point[0], 
      point[1] - other.point[1], point[2] - other.point[2]);
    return newPoint;
  }
  
  Point scalePoint(float scale) {
    Point newPoint = new Point(scale*point[0], scale*point[1], scale*point[2]);
    return newPoint;
  }
  
  float distance(Point other) {
    return(sqrt(pow(x() - other.x(), 2) + pow(y() - other.y(), 2) + pow(z() - other.z(), 2)));
  }
  
  float dotProduct(Point other) {
    return(x()*other.x() + y()*other.y() + z()*other.z());
  }
  
  Normal unit() {
    return(new Normal(x(), y(), z()));
  }
  
  Point clone() {
    return(new Point(x(), y(), z()));
  }
  
  String toString() {
    return("[" + x() + "," + y() + "," + z() + "]");
  }
}
