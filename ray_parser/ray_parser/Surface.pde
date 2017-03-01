class Surface {
  public float[] diffuse;
  public float[] ambient;
  public float[] specular;
  public float P;
  public float Krefl;
  
  Surface (float Cdr, float Cdg, float Cdb, float Car, float Cag, float Cab, float Csr, float Csg, float Csb, float P, float Krefl) {
    diffuse = new float[3];
    ambient = new float[3];
    specular = new float[3];
    diffuse[0] = Cdr;
    diffuse[1] = Cdg;
    diffuse[2] = Cdb;
    ambient[0] = Car;
    ambient[1] = Cag;
    ambient[2] = Cab;
    specular[0] = Csr;
    specular[1] = Csg;
    specular[2] = Csb;
    this.P = P;
    this.Krefl = Krefl;
  }
  
  Surface clone() {
    return(new Surface(Cdr(), Cdg(), Cdb(), Car(), Cag(), Cab(), Csr(), Csg(), Csb(), P(), Krefl()));
  }

  float Cdr() {
    return diffuse[0];
  }
  
  float Cdg() {
    return diffuse[1];
  }
  
  float Cdb() {
    return diffuse[2];
  }
  
  float Car() {
    return ambient[0];
  }
  
  float Cag() {
    return ambient[1];
  }
  
  float Cab() {
    return ambient[2];
  }
  
  float Csr() {
    return specular[0];
  }
  
  float Csg() {
    return specular[1];
  }
  
  float Csb() {
    return specular[2];
  }
  
  float P() {
    return P;
  }
  
  float Krefl() {
    return Krefl;
  }
}
