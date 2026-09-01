import { useEffect, useState } from 'react'
import './App.css'

const API_URL = 'http://localhost:8080/api/products'

const emptyForm = {
  name: '',
  sku: '',
  description: '',
  quantity: 0,
  price: '',
  reorderLevel: 5,
  warehouse: '',
  zone: '',
  aisle: '',
  rack: '',
  shelf: '',
  bin: '',
}

function getStatus(quantity, reorderLevel) {
  if (quantity === 0) {
    return {
      label: 'Out of Stock',
      className: 'out-stock',
    }
  }

  if (quantity <= reorderLevel) {
    return {
      label: 'Low Stock',
      className: 'low-stock',
    }
  }

  return {
    label: 'In Stock',
    className: 'in-stock',
  }
}

function App() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
const [form, setForm] = useState(emptyForm)
const [saving, setSaving] = useState(false)
const [editingProductId, setEditingProductId] = useState(null)
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [movementType, setMovementType] = useState('')
  const [movementQuantity, setMovementQuantity] = useState('')
  const [movementSaving, setMovementSaving] = useState(false)
  const [movements, setMovements] = useState([])
  const [showMovements, setShowMovements] = useState(false)

  function loadProducts() {
    setLoading(true)

    fetch(API_URL)
      .then((response) => {
        if (!response.ok) {
          throw new Error("HTTP error: " + response.status)
        }

        return response.json()
      })
      .then((data) => {
        setProducts(data)
        setError('')
      })
      .catch((err) => {
        console.error('Failed to load products:', err)
        setError('Unable to connect to the inventory server.')
      })
      .finally(() => {
        setLoading(false)
      })
  }
 useEffect(() => {
  fetch(API_URL)
    .then((response) => {
      if (!response.ok) {
        throw new Error('HTTP error: ' + response.status)
      }

      return response.json()
    })
    .then((data) => {
      setProducts(data)
      setError('')
    })
    .catch((err) => {
      console.error('Failed to load products:', err)
      setError('Unable to connect to the inventory server.')
    })
    .finally(() => {
      setLoading(false)
    })
}, [])

  function openMovement(product, type) {
    setSelectedProduct(product)
    setMovementType(type)
    setMovementQuantity('')
    setError('')
  }

  function closeMovement() {
    setSelectedProduct(null)
    setMovementType('')
    setMovementQuantity('')
  }

  function handleMovementSubmit(event) {
    event.preventDefault()

    const quantity = Number(movementQuantity)

    if (!quantity || quantity <= 0) {
      setError('Quantity must be greater than zero.')
      return
    }

    if (
      movementType === 'ISSUE' &&
      quantity > selectedProduct.quantity
    ) {
      setError(
        `Insufficient stock. Available: ${selectedProduct.quantity}`,
      )
      return
    }

    setMovementSaving(true)
    setError('')

    const endpoint =
      movementType === 'RECEIVE'
        ? `${API_URL}/${selectedProduct.id}/stock/receive`
        : `${API_URL}/${selectedProduct.id}/stock/issue`

    fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        quantity,
      }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error('HTTP error: ' + response.status)
        }

        return response.json()
      })
      .then(() => {
        closeMovement()
        loadProducts()
      })
      .catch((err) => {
        console.error('Failed to update stock:', err)
        setError('Unable to update stock.')
      })
      .finally(() => {
        setMovementSaving(false)
      })
  }

  function loadMovements(productId) {
    fetch(`${API_URL}/${productId}/movements`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('HTTP error: ' + response.status)
        }

        return response.json()
      })
      .then((data) => {
        setMovements(data)
        setShowMovements(true)
        setError('')
      })
      .catch((err) => {
        console.error('Failed to load movements:', err)
        setError('Unable to load stock movement history.')
      })
  }
  function handleChange(event) {
    const { name, value } = event.target

    setForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

 function handleSubmit(event) {
  event.preventDefault()
  setSaving(true)
  setError('')

  const product = {
    ...form,
    quantity: Number(form.quantity),
    price: Number(form.price),
    reorderLevel: Number(form.reorderLevel),
  }

  const isEditing = editingProductId !== null

  const url = isEditing
    ? `${API_URL}/${editingProductId}`
    : API_URL

  const method = isEditing ? 'PUT' : 'POST'

  fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(product),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error('HTTP error: ' + response.status)
      }

      return response.json()
    })
    .then(() => {
      setForm(emptyForm)
      setShowForm(false)
      setEditingProductId(null)
      loadProducts()
    })
    .catch((err) => {
      console.error('Failed to save product:', err)
      setError(
        isEditing
          ? 'Unable to update the product.'
          : 'Unable to create the product.',
      )
    })
    .finally(() => {
      setSaving(false)
    })
}

function handleEdit(product) {
  setForm({
    name: product.name || '',
    sku: product.sku || '',
    description: product.description || '',
    quantity: product.quantity ?? 0,
    price: product.price ?? '',
    reorderLevel: product.reorderLevel ?? 5,
    warehouse: product.warehouse || '',
    zone: product.zone || '',
    aisle: product.aisle || '',
    rack: product.rack || '',
    shelf: product.shelf || '',
    bin: product.bin || '',
  })

  setEditingProductId(product.id)
  setShowForm(true)
  setError('')
}

function handleDelete(product) {
  const confirmed = window.confirm(
    `Delete "${product.name}"? This action cannot be undone.`,
  )

  if (!confirmed) {
    return
  }

  setError('')

  fetch(`${API_URL}/${product.id}`, {
    method: 'DELETE',
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error('HTTP error: ' + response.status)
      }

      loadProducts()
    })
    .catch((err) => {
      console.error('Failed to delete product:', err)
      setError('Unable to delete the product.')
    })
}


  const totalProducts = products.length

  const lowStock = products.filter(
    (product) =>
      product.quantity > 0 &&
      product.quantity <= product.reorderLevel,
  ).length

  const outOfStock = products.filter(
    (product) => product.quantity === 0,
  ).length

  const totalInventoryValue = products.reduce(
    (total, product) =>
      total + Number(product.price || 0) * Number(product.quantity || 0),
    0,
  )

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Smart Inventory Manager</h1>
          <p>Inventory management dashboard</p>
        </div>

        <button
          type="button"
          className="primary-button"
          onClick={() => setShowForm(true)}
        >
          + Add Product
        </button>
      </header>

      <main className="dashboard">
        <section className="stats-grid">
          <div className="stat-card">
            <span>Total Products</span>
            <strong>{totalProducts}</strong>
          </div>

          <div className="stat-card">
            <span>Low Stock</span>
            <strong>{lowStock}</strong>
          </div>

          <div className="stat-card">
            <span>Out of Stock</span>
            <strong>{outOfStock}</strong>
          </div>

          <div className="stat-card">
            <span>Total Inventory Value</span>
            <strong>
              $
              {totalInventoryValue.toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
              })}
            </strong>
          </div>
        </section>
        {selectedProduct && (
          <section className="product-form-section">
            <div className="section-header">
              <div>
                <h2>
                  {movementType === 'RECEIVE'
                    ? 'Receive Stock'
                    : 'Issue Stock'}
                </h2>
                <p>
                  {selectedProduct.name} — Current quantity:{' '}
                  {selectedProduct.quantity}
                </p>
              </div>
            </div>

            <form
              className="product-form"
              onSubmit={handleMovementSubmit}
            >
              <div className="form-grid">
                <label>
                  Quantity
                  <input
                    type="number"
                    min="1"
                    value={movementQuantity}
                    onChange={(event) =>
                      setMovementQuantity(event.target.value)
                    }
                    required
                    autoFocus
                  />
                </label>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={closeMovement}
                  disabled={movementSaving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="primary-button"
                  disabled={movementSaving}
                >
                  {movementSaving
                    ? 'Saving...'
                    : movementType === 'RECEIVE'
                      ? 'Receive Stock'
                      : 'Issue Stock'}
                </button>
              </div>
            </form>
          </section>
        )}

        {showForm && (
          <section className="product-form-section">
            <div className="section-header">
              <div>
                <h2>{editingProductId ? 'Edit Product' : 'Add Product'}</h2>
<p>
  {editingProductId
    ? 'Update the product information below.'
    : 'Enter the product information below.'}
</p>
              </div>
            </div>

            <form className="product-form" onSubmit={handleSubmit}>
              <div className="form-grid">
                <label>
                  Product Name
                  <input
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    required
                  />
                </label>

                <label>
                  SKU
                  <input
                    name="sku"
                    value={form.sku}
                    onChange={handleChange}
                    required
                  />
                </label>

                <label>
                  Quantity
                  <input
                    type="number"
                    name="quantity"
                    min="0"
                    value={form.quantity}
                    onChange={handleChange}
                    required
                  />
                </label>

                <label>
                  Price
                  <input
                    type="number"
                    name="price"
                    min="0"
                    step="0.01"
                    value={form.price}
                    onChange={handleChange}
                    required
                  />
                </label>

                <label>
                  Reorder Level
                  <input
                    type="number"
                    name="reorderLevel"
                    min="0"
                    value={form.reorderLevel}
                    onChange={handleChange}
                    required
                  />
                </label>

                <label>
                  Warehouse
                  <input
                    name="warehouse"
                    value={form.warehouse}
                    onChange={handleChange}
                  />
                </label>

                <label>
                  Zone
                  <input
                    name="zone"
                    value={form.zone}
                    onChange={handleChange}
                  />
                </label>

                <label>
                  Aisle
                  <input
                    name="aisle"
                    value={form.aisle}
                    onChange={handleChange}
                  />
                </label>

                <label>
                  Rack
                  <input
                    name="rack"
                    value={form.rack}
                    onChange={handleChange}
                  />
                </label>

                <label>
                  Shelf
                  <input
                    name="shelf"
                    value={form.shelf}
                    onChange={handleChange}
                  />
                </label>

                <label>
                  Bin
                  <input
                    name="bin"
                    value={form.bin}
                    onChange={handleChange}
                  />
                </label>

                <label className="description-field">
                  Description
                  <textarea
                    name="description"
                    value={form.description}
                    onChange={handleChange}
                    rows="3"
                  />
                </label>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="secondary-button"
                 onClick={() => {
  setShowForm(false)
  setForm(emptyForm)
  setEditingProductId(null)
  setError('')
}}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="primary-button"
                  disabled={saving}
                >
                  {saving
  ? 'Saving...'
  : editingProductId
    ? 'Update Product'
    : 'Save Product'}
                </button>
              </div>
            </form>
          </section>
        )}

        <section className="inventory-section">
          <div className="section-header">
            <div>
              <h2>Inventory</h2>
              <p>Manage your products and stock levels.</p>
            </div>
          </div>

          {loading && (
            <div className="table-message">
              Loading inventory...
            </div>
          )}

          {error && (
            <div className="table-message error-message">
              {error}
            </div>
          )}

          {!loading && (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Quantity</th>
                    <th>Price</th>
                                        <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {products.map((product) => {
                    const status = getStatus(
                      product.quantity,
                      product.reorderLevel,
                    )

                    return (
                      <tr key={product.id}>
                        <td>
                          <strong>{product.name}</strong>
                        </td>
                        <td>{product.sku}</td>
                        <td>{product.quantity}</td>
                        <td>
                          ${Number(product.price).toFixed(2)}
                        </td>
                                                <td>
                          <span
                            className={"status " + status.className}
                          >
                            {status.label}
                          </span>
                        </td>

                        <td>
                          <div className="action-buttons">
                            <button
                              type="button"
                              className="small-button"
                              onClick={() =>
                                openMovement(product, 'RECEIVE')
                              }
                            >
                              Receive
                            </button>

                            <button
                              type="button"
                              className="small-button"
                              onClick={() =>
                                openMovement(product, 'ISSUE')
                              }
                            >
                              Issue
                            </button>

                            <button
                              type="button"
                              className="small-button"
                              onClick={() =>
                                loadMovements(product.id)
                              }
                            >
                              History
                            </button>
<button
  type="button"
  className="small-button"
  onClick={() => handleEdit(product)}
>
  Edit
</button>

<button
  type="button"
  className="small-button delete-button"
  onClick={() => handleDelete(product)}
>
  Delete
</button>

                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
        {showMovements && (
          <section className="inventory-section movement-history">
            <div className="section-header">
              <div>
                <h2>Stock Movement History</h2>
                <p>Recent stock received and issued.</p>
              </div>

              <button
                type="button"
                className="secondary-button"
                onClick={() => setShowMovements(false)}
              >
                Close
              </button>
            </div>

            <div className="table-container">
              {movements.length === 0 ? (
                <div className="table-message">
                  No stock movements found.
                </div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>Type</th>
                      <th>Quantity</th>
                      <th>Date</th>
                    </tr>
                  </thead>

                  <tbody>
                    {movements.map((movement) => (
                      <tr key={movement.id}>
                        <td>
                          <strong>{movement.product.name}</strong>
                        </td>

                        <td>
                          <span
                            className={
                              movement.type === 'RECEIVE'
                                ? 'status in-stock'
                                : 'status low-stock'
                            }
                          >
                            {movement.type}
                          </span>
                        </td>

                        <td>{movement.quantity}</td>

                        <td>
                          {new Date(
                            movement.createdAt,
                          ).toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </section>
        )}
      </main>
    </div>
  )
}

export default App



