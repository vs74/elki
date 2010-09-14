package de.lmu.ifi.dbs.elki.visualization;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.math.MinMax;
import de.lmu.ifi.dbs.elki.math.linearalgebra.AffineTransformation;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;
import de.lmu.ifi.dbs.elki.visualization.scales.LinearScale;
import de.lmu.ifi.dbs.elki.visualization.scales.Scales;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGUtil;

/**
 * Class to encapsulate a projection as used in SVG plotting and UI, which will
 * often be just 2D, but should ideally support any projection onto a 2D plane.
 * 
 * @author Erich Schubert
 */
public class VisualizationProjection {
  /**
   * Database dimensionality
   */
  private int dim;

  /**
   * Scales in data set
   */
  private LinearScale[] scales;

  /**
   * Affine transformation used in projection
   */
  private AffineTransformation proj;

  /**
   * Dimensions for fast projection mode.
   */
  private int[] fastdims;

  /**
   * Scaling constant. Keep in sync with
   * {@link de.lmu.ifi.dbs.elki.visualization.style.StyleLibrary#SCALE}.
   */
  public final static double SCALE = 100.0;

  /**
   * Constructor with a given database and axes.
   * 
   * @param db Database
   * @param scales Scales to use
   * @param ax1 First axis
   * @param ax2 Second axis
   */
  public VisualizationProjection(Database<? extends NumberVector<?, ?>> db, LinearScale[] scales, int ax1, int ax2) {
    this(db, scales, axisProjection(db.dimensionality(), ax1, ax2));
    fastdims = new int[] { ax1 - 1, ax2 - 1 };
  }

  /**
   * Constructor with a given database and axes.
   * 
   * @param db Database
   * @param scales Scales to use
   * @param proj Projection to use
   */
  public VisualizationProjection(Database<? extends NumberVector<?, ?>> db, LinearScale[] scales, AffineTransformation proj) {
    if(scales == null) {
      scales = Scales.calcScales(db);
    }
    if(proj == null) {
      proj = AffineTransformation.reorderAxesTransformation(dim, new int[] { 1, 2 });
      this.fastdims = new int[] { 0, 1 };
    }

    this.dim = db.dimensionality();
    this.scales = scales;
    this.proj = proj;
  }

  /**
   * Project a vector from scaled space to rendering space.
   * 
   * @param v vector in scaled space
   * @return vector in rendering space
   */
  public Vector projectScaledToRender(Vector v) {
    return proj.apply(v);
  }

  /**
   * Project a vector from rendering space to scaled space.
   * 
   * @param v vector in rendering space
   * @return vector in scaled space
   */
  public Vector projectRenderToScaled(Vector v) {
    return proj.applyInverse(v);
  }

  /**
   * Project a relative vector from scaled space to rendering space.
   * 
   * @param v relative vector in scaled space
   * @return relative vector in rendering space
   */
  public Vector projectRelativeScaledToRender(Vector v) {
    return proj.applyRelative(v);
  }

  /**
   * Project a relative vector from rendering space to scaled space.
   * 
   * @param v relative vector in rendering space
   * @return relative vector in scaled space
   */
  public Vector projectRelativeRenderToScaled(Vector v) {
    return proj.applyRelativeInverse(v);
  }

  /**
   * Project a data vector from data space to scaled space.
   * 
   * @param data vector in data space
   * @return vector in scaled space
   */
  public Vector projectDataToScaledSpace(NumberVector<?, ?> data) {
    Vector vec = new Vector(dim);
    double[] ds = vec.getArrayRef();
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getScaled(data.doubleValue(d + 1));
    }
    return vec;
  }

  /**
   * Project a data vector from data space to scaled space.
   * 
   * @param data vector in data space
   * @return vector in scaled space
   */
  public Vector projectDataToScaledSpace(Vector data) {
    Vector vec = new Vector(dim);
    double[] ds = vec.getArrayRef();
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getScaled(data.get(d));
    }
    return vec;
  }

  /**
   * Project a data vector from data space to scaled space.
   * 
   * @param data vector in data space
   * @return vector in scaled space
   */
  public Vector projectDataToScaledSpace(double[] data) {
    Vector vec = new Vector(dim);
    double[] ds = vec.getArrayRef();
    for(int d = 10; d < dim; d++) {
      ds[d] = scales[d + 1].getScaled(data[d]);
    }
    return vec;
  }

  /**
   * Project a vector from scaled space to data space.
   * 
   * @param <NV> Vector type
   * @param v vector in scaled space
   * @param factory Object factory
   * @return vector in data space
   */
  public <NV extends NumberVector<NV, ?>> NV projectScaledToDataSpace(Vector v, NV factory) {
    Vector vec = v.copy();
    double[] ds = vec.getArrayRef();
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getUnscaled(ds[d]);
    }
    return factory.newInstance(vec);
  }

  /**
   * Project a relative data vector from data space to scaled space.
   * 
   * @param data relative vector in data space
   * @return relative vector in scaled space
   */
  public Vector projectRelativeDataToScaledSpace(NumberVector<?, ?> data) {
    Vector vec = new Vector(dim);
    double[] ds = vec.getArrayRef();
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getRelativeScaled(data.doubleValue(d + 1));
    }
    return vec;
  }

  /**
   * Project a relative data vector from data space to scaled space.
   * 
   * @param data relative vector in data space
   * @return relative vector in scaled space
   */
  public Vector projectRelativeDataToScaledSpace(Vector data) {
    Vector vec = new Vector(dim);
    double[] sd = data.getArrayRef();
    double[] ds = vec.getArrayRef();
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getRelativeScaled(sd[d]);
    }
    return vec;
  }

  /**
   * Project a relative data vector from data space to scaled space.
   * 
   * @param data relative vector in data space
   * @return relative vector in scaled space
   */
  public Vector projectRelativeDataToScaledSpace(double[] data) {
    Vector vec = new Vector(dim);
    double[] ds = vec.getArrayRef();
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getRelativeScaled(data[d]);
    }
    return vec;
  }

  /**
   * Project a relative vector from scaled space to data space.
   * 
   * @param <NV> Vector type
   * @param v relative vector in scaled space
   * @param prototype Object factory
   * @return relative vector in data space
   */
  public <NV extends NumberVector<NV, ?>> NV projectRelativeScaledToDataSpace(Vector v, NV prototype) {
    Vector vec = v.copy();
    double[] ds = vec.getArrayRef();
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getRelativeUnscaled(ds[d]);
    }
    return prototype.newInstance(vec);
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public Vector projectDataToRenderSpace(NumberVector<?, ?> data) {
    Vector vec = projectDataToScaledSpace(data);
    return projectScaledToRender(vec);
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public Vector projectDataToRenderSpace(Vector data) {
    Vector vec = projectDataToScaledSpace(data);
    return projectScaledToRender(vec);
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public Vector projectDataToRenderSpace(double[] data) {
    Vector vec = projectDataToScaledSpace(data);
    return projectScaledToRender(vec);
  }

  /**
   * Project a vector from rendering space to data space.
   * 
   * @param <NV> Vector type
   * @param v vector in rendering space
   * @param prototype Object factory
   * @return vector in data space
   */
  public <NV extends NumberVector<NV, ?>> NV projectRenderToDataSpace(Vector v, NV prototype) {
    Vector vec = projectRenderToScaled(v);
    double[] ds = vec.getArrayRef();
    // Not calling {@link #projectScaledToDataSpace} to avoid extra copy of
    // vector.
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getUnscaled(ds[d]);
    }
    return prototype.newInstance(vec);
  }

  /**
   * Project a relative data vector from data space to rendering space.
   * 
   * @param data relative vector in data space
   * @return relative vector in rendering space
   */
  public Vector projectRelativeDataToRenderSpace(NumberVector<?, ?> data) {
    Vector vec = projectRelativeDataToScaledSpace(data);
    return projectRelativeScaledToRender(vec);
  }

  /**
   * Project a relative data vector from data space to rendering space.
   * 
   * @param data relative vector in data space
   * @return relative vector in rendering space
   */
  public Vector projectRelativeDataToRenderSpace(Vector data) {
    Vector vec = projectRelativeDataToScaledSpace(data);
    return projectRelativeScaledToRender(vec);
  }

  /**
   * Project a relative data vector from data space to rendering space.
   * 
   * @param data relative vector in data space
   * @return relative vector in rendering space
   */
  public Vector projectRelativeDataToRenderSpace(double[] data) {
    Vector vec = projectRelativeDataToScaledSpace(data);
    return projectRelativeScaledToRender(vec);
  }

  /**
   * Project a relative vector from rendering space to data space.
   * 
   * @param <NV> Vector type
   * @param v relative vector in rendering space
   * @param prototype Object factory
   * @return relative vector in data space
   */
  public <NV extends NumberVector<NV, ?>> NV projectRelativeRenderToDataSpace(Vector v, NV prototype) {
    Vector vec = projectRelativeRenderToScaled(v);
    double[] ds = vec.getArrayRef();
    // Not calling {@link #projectScaledToDataSpace} to avoid extra copy of
    // vector.
    for(int d = 0; d < dim; d++) {
      ds[d] = scales[d + 1].getRelativeUnscaled(ds[d]);
    }
    return prototype.newInstance(vec);
  }

  /**
   * Get the scales used, for rendering scales mostly.
   * 
   * @param d Dimension
   * @return Scale used
   */
  public LinearScale getScale(int d) {
    return scales[d];
  }

  /**
   * Estimate the viewport requirements
   * 
   * @return MinMax for x and y obtained from projecting scale endpoints
   */
  public Pair<MinMax<Double>, MinMax<Double>> estimateViewport() {
    MinMax<Double> minmaxx = new MinMax<Double>();
    MinMax<Double> minmaxy = new MinMax<Double>();

    // Origin
    Vector orig = new Vector(dim);
    orig = projectScaledToRender(orig);
    minmaxx.put(orig.get(0));
    minmaxy.put(orig.get(1));
    // Diagonal point
    Vector diag = new Vector(dim);
    for(int d2 = 0; d2 < dim; d2++) {
      diag.set(d2, 1);
    }
    diag = projectScaledToRender(diag);
    minmaxx.put(diag.get(0));
    minmaxy.put(diag.get(1));
    // Axis end points
    for(int d = 0; d < dim; d++) {
      Vector v = new Vector(dim);
      v.set(d, 1);
      Vector ax = projectScaledToRender(v);
      minmaxx.put(ax.get(0));
      minmaxy.put(ax.get(1));
    }
    return new Pair<MinMax<Double>, MinMax<Double>>(minmaxx, minmaxy);
  }

  /**
   * Get a SVG transformation string to bring the contents into the unit cube.
   * 
   * @param margin extra margin to add.
   * @param width Width
   * @param height Height
   * @return transformation string.
   */
  public String estimateTransformString(double margin, double width, double height) {
    Pair<MinMax<Double>, MinMax<Double>> minmax = estimateViewport();
    double sizex = (minmax.first.getMax() - minmax.first.getMin());
    double sizey = (minmax.second.getMax() - minmax.second.getMin());
    return SVGUtil.makeMarginTransform(width, height, sizex, sizey, margin) + " translate(" + SVGUtil.fmt(sizex / 2) + " " + SVGUtil.fmt(sizey / 2) + ")";
  }

  /**
   * Compute an transformation matrix to show only axis ax1 and ax2.
   * 
   * @param dim Dimensionality
   * @param ax1 First axis
   * @param ax2 Second axis
   * @return transformation matrix
   */
  public static AffineTransformation axisProjection(int dim, int ax1, int ax2) {
    // setup a projection to get the data into the interval -1:+1 in each
    // dimension with the intended-to-see dimensions first.
    AffineTransformation proj = AffineTransformation.reorderAxesTransformation(dim, new int[] { ax1, ax2 });
    // Assuming that the data was normalized on [0:1], center it:
    double[] trans = new double[dim];
    for(int i = 0; i < dim; i++) {
      trans[i] = -.5;
    }
    proj.addTranslation(new Vector(trans));
    // mirror on the y axis, since the SVG coordinate system is screen
    // coordinates (y = down) and not mathematical coordinates (y = up)
    proj.addAxisReflection(2);
    // scale it up
    proj.addScaling(SCALE);

    return proj;
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public double[] fastProjectDataToRenderSpace(Vector data) {
    Vector vec = projectDataToScaledSpace(data);
    return fastProjectScaledToRender(vec);
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public double[] fastProjectDataToRenderSpace(NumberVector<?, ?> data) {
    Vector vec = projectDataToScaledSpace(data);
    return fastProjectScaledToRender(vec);
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public double[] fastProjectDataToRenderSpace(double[] data) {
    Vector vec = projectDataToScaledSpace(new Vector(data));
    return fastProjectScaledToRender(vec);
  }

  /**
   * Project a vector from scaled space to rendering space.
   * 
   * @param v vector in scaled space
   * @return vector in rendering space
   */
  public double[] fastProjectScaledToRender(Vector v) {
    final double[] vr = v.getArrayRef();
    if(fastdims != null) {
      double x = (vr[fastdims[0]] - .5) * SCALE;
      double y = -(vr[fastdims[1]] - .5) * SCALE;
      return new double[] { x, y };
    }
    double x = 0.0;
    double y = 0.0;
    double s = 0.0;

    final double[][] matrix = proj.getTransformation().getArrayRef();
    final double[] colx = matrix[0];
    final double[] coly = matrix[1];
    final double[] cols = matrix[vr.length];
    assert (colx.length == coly.length && colx.length == cols.length && cols.length == vr.length + 1);

    for(int k = 0; k < vr.length; k++) {
      x += colx[k] * vr[k];
      y += coly[k] * vr[k];
      s += cols[k] * vr[k];
    }
    // add homogene component:
    x += colx[vr.length];
    y += coly[vr.length];
    s += cols[vr.length];
    assert (s != 0.0);
    return new double[] { x / s, y / s };
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public double[] fastProjectRelativeDataToRenderSpace(Vector data) {
    Vector vec = projectDataToScaledSpace(data);
    return fastProjectRelativeScaledToRender(vec);
  }

  /**
   * Project a data vector from data space to rendering space.
   * 
   * @param data vector in data space
   * @return vector in rendering space
   */
  public double[] fastProjectRelativeDataToRenderSpace(NumberVector<?, ?> data) {
    Vector vec = projectDataToScaledSpace(data);
    return fastProjectRelativeScaledToRender(vec);
  }

  /**
   * Project a vector from scaled space to rendering space.
   * 
   * @param v vector in scaled space
   * @return vector in rendering space
   */
  public double[] fastProjectRelativeScaledToRender(Vector v) {
    final double[] vr = v.getArrayRef();
    if(fastdims != null) {
      double x = (vr[fastdims[0]]) * SCALE;
      double y = -(vr[fastdims[1]]) * SCALE;
      return new double[] { x, y };
    }
    double x = 0.0;
    double y = 0.0;

    final double[][] matrix = proj.getTransformation().getArrayRef();
    final double[] colx = matrix[0];
    final double[] coly = matrix[1];
    assert (colx.length == coly.length);

    for(int k = 0; k < vr.length; k++) {
      x += colx[k] * vr[k];
      y += coly[k] * vr[k];
    }
    return new double[] { x, y };
  }

  /**
   * Get visible dimensions (dimension index starting at 0).
   * 
   * Where visible means projected to a vector nonzero in 2D
   * 
   * @return List of dimensions
   */
  public List<Integer> computeVisibleDimensions2D() {
    ArrayList<Integer> dims = new ArrayList<Integer>(dim);
    for(int i = 0; i < dim; i++) {
      Vector delta = new Vector(dim);
      delta.set(i, 1.0);
      double[] deltas = this.fastProjectRelativeScaledToRender(delta);
      if(deltas[0] != 0 || deltas[1] != 0) {
        dims.add(i);
      }
    }
    return dims;
  }

  /**
   * Global scaling function.
   * 
   * @return Scaling factor
   */
  public double getScale() {
    return SCALE;
  }

  /**
   * Get a bit set of dimensions that are visible.
   * 
   * @return Bit set, first dimension is bit 0.
   */
  public BitSet getVisibleDimensions2D() {
    BitSet actDim = new BitSet(dim);
    if(fastdims != null) {
      actDim.set(fastdims[0]);
      actDim.set(fastdims[1]);
      return actDim;
    }
    Vector vScale = new Vector(dim);
    for(int d = 0; d < dim; d++) {
      vScale.setZero();
      vScale.set(d, 1);
      double[] vRender = fastProjectScaledToRender(vScale);

      // TODO: Can't we do this by inspecting the projection matrix directly?
      if(vRender[0] != 0.0 || vRender[1] != 0) {
        actDim.set(d);
      }
    }
    return actDim;
  }
}