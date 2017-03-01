class Shape {
  Surface surface;
  
  Shape (Surface surface) {
    this.surface = surface;
  }
  
  CollisionInfo collision(Ray ray) {
    return new CollisionInfo();
  }
}
