class Normal {
  public float[] normal;
  
  Normal (float x, float y, float z) {
    normal = new float[3];
    normal[0] = x;
    normal[1] = y;
    normal[2] = z;
    unit();
  }
  
  Normal(Point point) {
    normal = new float[3];
    normal[0] = point.point[0];
    normal[1] = point.point[1];
    normal[2] = point.point[2];
    unit();
  }
  
  float x() {
    return normal[0];
  }
  
  float y() {
    return normal[1];
  }
  
  float z() {
    return normal[2];
  }
  
  void unit() {
    float magnitude = (float) Math.sqrt((double)(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]));
    normal[0] = normal[0]/magnitude;
    normal[1] = normal[1]/magnitude;
    normal[2] = normal[2]/magnitude;
  }
  
  Point scaleNormal(float scale) {
    Point newDirection = new Point(scale*normal[0], scale*normal[1], scale*normal[2]);
    return newDirection;
  }
  
  Point addPoint(Point point) {
    Point newPoint = new Point(normal[0]+point.x(), normal[1]+point.y(), normal[2]+point.z());
    return point;
  }
  
  Point subtract(Normal other) {
    return(new Point(x()-other.x(), y()-other.y(), z()-other.z()));
  }
  
  Point subtract(Point point) {
    return(new Point(x()-point.x(), y()-point.y(), z()-point.z()));
  }
  
  float dotProduct(Normal otherNormal) {
    return(normal[0]*otherNormal.x() + normal[1]*otherNormal.y() + normal[2]*otherNormal.z());
  }
  
  Ray reflect(Ray incoming, Point location) {
    Ray outgoing;
    Normal direction = reflect(incoming);
    outgoing = new Ray(location, direction);
    return outgoing;
  }
  
  Normal reflect(Ray incoming) {
    Normal outgoing = new Normal(incoming.direction.subtract(scaleNormal(2*dotProduct(incoming.direction))));
    return outgoing;
  }
  
  Point toPoint() {
    return(new Point(x(), y(), z()));
  }
  
  Normal clone() {
    return(new Normal(x(), y(), z()));
  }
  
  Normal flip() {
    return(new Normal(-x(), -y(), -z()));
  }
  
  String toString() {
    return("[" + x() + "," + y() + "," + z() + "]");
  }
}
