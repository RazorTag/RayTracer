class Triangle extends Shape {
  ArrayList<Point> vertices;
  Point Q;
  Normal N;
  
  Triangle(Surface surface, ArrayList<Point> triangle) {
    super(surface);
    vertices = new ArrayList<Point>();
    for(int i = 0; i < 3; i++) {
      vertices.add(triangle.get(i).clone());
    }
    setQN();
  }
  
  CollisionInfo collision(Ray ray) {
    CollisionInfo planeInfo = plane(ray);
    if(planeInfo.t < 0.0) {return (new CollisionInfo());}
    if(halfPlane(planeInfo)) {return planeInfo;}
    return (new CollisionInfo());
  }
  
  //returns true if the point where the ray collides with the plane is inside of the triangle
  boolean halfPlane(CollisionInfo planeInfo) {
    Point P = planeInfo.location.clone();
    Point A = vertices.get(0).clone();
    Point B = vertices.get(1).clone();
    Point C = vertices.get(2).clone();
    boolean sameSidePA = sameSide(P, A, C, B);
    boolean sameSidePB = sameSide(P, B, C, A);
    boolean sameSidePC = sameSide(P, C, A, B);
    return (sameSidePA && sameSidePB && sameSidePC);
  }
  
  //Returns true if P and A are on the same side of line BC
  boolean sameSide(Point P, Point A, Point B, Point C) {
    Point lineBC = C.subtractPoints(B);
    Point lineBA = A.subtractPoints(B);
    Point lineBP = P.subtractPoints(B);
    Point productA = crossProduct(lineBC, lineBA);
    Point productP = crossProduct(lineBC, lineBP);
    float direction = productA.dotProduct(productP);
    if(direction >= 0.0) {return true;}
    else {return false;}
  }
  
  //Returns CollisionInfo for the plane (chooses between 2 possible normals)
  CollisionInfo plane(Ray ray) {
    Point location;
    Point QO = ray.origin.subtractPoints(Q);
    Point normal = N.toPoint();
    float denominator = (ray.direction.toPoint().dotProduct(normal));
    if(denominator == 0.0) {return (new CollisionInfo());}
    float t = ((-(QO.dotProduct(normal))) / denominator);
    if(t < 0.0) {return (new CollisionInfo());}
    location = ray.origin.addPoints(ray.scaleDirection(t));
    Normal newNormal = chooseNormal(N, ray);
    return (new CollisionInfo(t, location, newNormal, surface));
  }
  
  Normal chooseNormal(Normal normal, Ray ray) {
    float up = ray.direction.dotProduct(N);
    float down = ray.direction.dotProduct(N.flip());
    if(up <= down) {return normal.clone();}
    else {return normal.flip();}
  }
  
  void setQN() {
    Q = vertices.get(0).clone();
    crossProduct();
  }
  
  // (C - B) X (A - B)
  Point crossProduct(Point BC, Point BA) {
    float i = BC.y() * BA.z() - BC.z() * BA.y();
    float j = BC.x() * BA.z() - BC.z() * BA.x();
    float k = BC.x() * BA.y() - BC.y() * BA.x();
    Point product = new Point(i, j, k);
    return product;
  }
  
  // (vertex3 - vertex1) X (vertex2 - vertex1)
  void crossProduct() {
    Point line13 = vertices.get(2).subtractPoints(Q);
    Point line12 = vertices.get(1).subtractPoints(Q);
    float i = (line13.y() * line12.z()) - (line13.z() * line12.y());
    float j = -((line13.x() * line12.z()) - (line13.z() * line12.x()));
    float k = (line13.x() * line12.y()) - (line13.y() * line12.x());
    N = new Normal(i, j, k);
  }
  
  void add(Point p) {
    if(vertices.size() < 3) {
      vertices.add(p);
    }
    else {
      println("Too many vertices for a triangle");
    }
  }
  
  void add(float x, float y, float z) {
    Point p = new Point(x, y, z);
    add(p);
  }
  
  Point vertexA() {
    return vertices.get(0);
  }
  
  Point vertexB() {
    return vertices.get(1);
  }
  
  Point vertexC() {
    return vertices.get(2);
  }
  
  String toString() {
    String s = "";
    for(Point point : vertices) {
      s += (point.toString() + "\t");
    }
    return s;
  }
}
