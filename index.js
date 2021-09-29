import React, { Component } from 'react';
import {
  View,
  requireNativeComponent,
  ViewPropTypes
} from 'react-native';
import { bool, string, number, array, shape, arrayOf, func } from 'prop-types';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

class ImageSequence extends Component {

  _onFinish = (event) => {
    this.props.onFinish(event.nativeEvent)
  }

  render() {
    let normalized = this.props.images.map(resolveAssetSource);

    // reorder elements if start-index is different from 0 (beginning)
    if (this.props.startFrameIndex !== 0) {
      normalized = [...normalized.slice(this.props.startFrameIndex), ...normalized.slice(0, this.props.startFrameIndex)];
    }

    return (
      <RCTImageSequence
        {...this.props}
        onFinish={this._onFinish}
        images={normalized} />
    );
  }
}

ImageSequence.defaultProps = {
  startFrameIndex: 0,
  framesPerSecond: 24,
  loop: true,
};

ImageSequence.propTypes = {
  startFrameIndex: number,
  images: array.isRequired,
  framesPerSecond: number,
  loop: bool,
  onFinish: func
};

const RCTImageSequence = requireNativeComponent('RCTImageSequence', {
  propTypes: {
    ...ViewPropTypes,
    images: arrayOf(shape({
      uri: string.isRequired
    })).isRequired,
    framesPerSecond: number,
    loop: bool,
    onFinish: func
  },
});

export default ImageSequence;
