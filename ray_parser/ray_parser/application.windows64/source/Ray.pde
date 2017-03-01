class Ray {
  public Point origin;
  public Normal direction;
  
  Ray (Point origin, Normal direction) {
    this.origin = origin;
    this.direction = direction;
  }
  
  Ray (Point origin, Point direction) {
    this.origin = origin;
    this.direction = new Normal(direction);
  }
  
  Ray (float x0, float y0, float z0, float x1, float y1, float z1) {
    this(new Point(x0, y0, z0), new Normal(x1, y1, z1));
  }
  
  float dotProduct(Ray ray) {
    return(direction.normal[0]*ray.direction.normal[0]
      + direction.normal[1]*ray.direction.normal[1] 
      + direction.normal[2]*ray.direction.normal[2]);
  }
  
  Point scaleDirection(float scale) {
    return(direction.scaleNormal(scale));
  }
  
  //returns t = -1.0 if no collision occurs
  CollisionInfo nearestCollision(ArrayList<Shape> shapes) {
    CollisionInfo info;
    CollisionInfo nearestCollision = new CollisionInfo();
    for(Shape shape : shapes) {
      info = shape.collision(this);
      if((nearestCollision.t < 0.0 && info.t > 0.0) || (info.t < nearestCollision.t && info.t > 0.0)) { //found a new closest collision point
        nearestCollision = info.clone();
      }
    }
    return nearestCollision;
  }
  
  Normal unit() {
    float magnitude = sqrt(pow(direction.normal[0], 2.0) 
      + pow(direction.normal[1], 2.0) + pow(direction.normal[2], 2.0));
    Normal normal = new Normal(direction.normal[0]/magnitude, 
      direction.normal[1]/magnitude, direction.normal[2]/magnitude);
    return normal;
  }
  
  Ray flip() {
    Ray newRay = new Ray(origin, direction.scaleNormal(-1.0));
    return newRay;
  }
  
  String toString() {
    return("Origin: " + "[" + origin.x() + "," + origin.y() + "," + origin.z() + "]"
      + "\t\tDirection: " + "[" + direction.x() + "," + direction.y() + "," + direction.z() + "]");
  }
}
