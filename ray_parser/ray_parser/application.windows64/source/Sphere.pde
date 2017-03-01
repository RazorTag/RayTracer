class Sphere extends Shape {
  float radius;
  Point center;
  
  Sphere (Surface surface, float radius, Point center) {
    super(surface);
    this.radius = radius;
    this.center = center;
  }
  
  Sphere (Surface surface, float radius, float x, float y, float z) {
    this(surface, radius, new Point(x, y, z));
  }
  
  @Override
  CollisionInfo collision(Ray ray) {
    float t;
    Point location;
    Normal normal;
    float a = ray.dotProduct(ray);
    Point origin = new Point(ray.origin.point[0], ray.origin.point[1], ray.origin.point[2]);
    Point CO = ray.origin.subtractPoints(center); //vector from center of sphere to origin of ray
    float b = CO.dotProduct(ray.scaleDirection(2.0));
    float c = CO.dotProduct(CO) - radius*radius;
    float b24ac = b*b - 4.0*a*c;
    if(b24ac < 0) {return (new CollisionInfo());}
    else if(b24ac == 0) {t = calcT(true, a, b, c, b24ac);}
    else {t = minPos(calcT(true, a, b, c, b24ac), calcT(false, a, b, c, b24ac));}
    location = origin.addPoints(ray.scaleDirection(t));
    normal = new Normal(location.subtractPoints(center));
    CollisionInfo collision = new CollisionInfo(t, location, normal, surface);
    return collision;
  }
  
  //returns the smallest positive float or -1.0 if both are negative
  float minPos(float one, float two) {
    if(one < 0.0 && two < 0.0) {return -1.0;}
    if(one < 0.0) {return two;}
    if(two < 0.0) {return one;}
    else {return min(one, two);}
  }
  
  //quadratic formula (true for addition, false for subtraction)
  float calcT(boolean plus, float a, float b, float c, float b24ac) {
    float t;
    if(plus) {
      t = (-b + sqrt(b24ac))/(2.0*a);
    }
    else {
      t = (-b - sqrt(b24ac))/(2.0*a);
    }
    return t;
  }
  
  float radius() {
    return radius;
  }
  
  float x() {
     return center.x(); 
  }
  
  float y() {
     return center.y(); 
  }
  
  float z() {
     return center.z(); 
  }
  
  String toString() {
    return("Sphere with radius " + radius + " at " + center.toString());
  }
}
