class Color {
  float[] colors;
  
  Color (float red, float green, float blue) {
    colors = new float[3];
    colors[0] = red;
    colors[1] = green;
    colors[2] = blue;
    regular();
  }
  
  float getRed() {
    return colors[0];
  }
  
  float getGreen() {
    return colors[1];
  }

  float getBlue() {
    return colors[2];
  }
  
  color getColor() {
    return color(colors[0], colors[1], colors[2]);
  }
  
  void addColor(Color other) {
    for(int i = 0; i < colors.length; i++) {
      colors[i] += other.colors[i];
    }
    regular();
  }
  
  Color scaleColor(float scale) {
    Color newColor = new Color(colors[0]*scale, colors[1]*scale, colors[2]*scale);
    return newColor;
  }
  
  void regular() {
    for(Float value : colors) {
      value = min(value, 255.0);
      value = max(value, 0.0);
    }
  }
  
  String toString() {
    return("[" + getRed() + "," + getGreen() + "," + getBlue() + "]");
  }
}
